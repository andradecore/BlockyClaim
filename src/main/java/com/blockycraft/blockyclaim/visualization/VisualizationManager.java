package com.blockycraft.blockyclaim.visualization;

import com.blockycraft.blockyclaim.BlockyClaim;
import com.blockycraft.blockyclaim.data.Claim;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VisualizationManager {

    private final BlockyClaim plugin;
    private final Map<String, Set<OriginalBlockState>> activeVisualizations = new HashMap<>();
    private final Map<String, String> currentlyVisualizing = new HashMap<>();

    public VisualizationManager(BlockyClaim plugin) {
        this.plugin = plugin;
    }

    public void start() {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                checkPlayers();
            }
        }, 0L, 10L);
    }

    private void checkPlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Material claimTool = plugin.getConfigManager().getFerramentaClaim();
            Claim claim = plugin.getClaimManager().getNearbyClaim(player.getLocation(), 10); // Claim próxima

            boolean isHoldingTool = player.getItemInHand().getType() == claimTool;

            if (isHoldingTool && claim != null) {
                Location playerLoc = player.getLocation();

                int dx = Math.max(claim.getMinX() - playerLoc.getBlockX(), playerLoc.getBlockX() - claim.getMaxX());
                int dz = Math.max(claim.getMinZ() - playerLoc.getBlockZ(), playerLoc.getBlockZ() - claim.getMaxZ());
                dx = Math.max(dx, 0);
                dz = Math.max(dz, 0);
                double distance = Math.sqrt(dx * dx + dz * dz);

                if (distance <= 10) {
                    String claimId = claim.getOwnerName() + ";" + claim.getClaimName();
                    if (!claimId.equals(currentlyVisualizing.get(player.getName()))) {
                        showBorders(player, claim);
                    }
                } else {
                    if (currentlyVisualizing.containsKey(player.getName())) {
                        clearBorders(player);
                    }
                }
            } else {
                if (currentlyVisualizing.containsKey(player.getName())) {
                    clearBorders(player);
                }
            }
        }
    }

    private void showBorders(Player player, Claim claim) {
        clearBorders(player);

        World world = player.getWorld();
        int y = player.getLocation().getBlockY() - 1; // 1 bloco abaixo

        // CANTOS: minX/minZ, maxX/minZ, minX/maxZ, maxX/maxZ
        List<Location> cornerLocations = new ArrayList<>();
        cornerLocations.add(new Location(world, claim.getMinX(), y, claim.getMinZ())); // Inferior esquerdo
        cornerLocations.add(new Location(world, claim.getMaxX(), y, claim.getMinZ())); // Inferior direito
        cornerLocations.add(new Location(world, claim.getMinX(), y, claim.getMaxZ())); // Superior esquerdo
        cornerLocations.add(new Location(world, claim.getMaxX(), y, claim.getMaxZ())); // Superior direito

        Set<OriginalBlockState> originalBlocks = new HashSet<>();

        for (Location loc : cornerLocations) {
            int x = loc.getBlockX();
            int z = loc.getBlockZ();

            // Glowstone no canto
            Location glowstoneLoc = new Location(world, x, y, z);
            Block realBlockGlow = world.getBlockAt(glowstoneLoc);
            originalBlocks.add(new OriginalBlockState(glowstoneLoc, realBlockGlow.getType(), realBlockGlow.getData()));
            player.sendBlockChange(glowstoneLoc, Material.GLOWSTONE, (byte) 0);

            // Lógica dos blocos de ouro apontando para dentro:
            Location goldLocA, goldLocB;
            if (x == claim.getMinX() && z == claim.getMinZ()) {
                // Inferior esquerdo: aponta +X e +Z
                goldLocA = new Location(world, x + 1, y, z);
                goldLocB = new Location(world, x, y, z + 1);
            } else if (x == claim.getMaxX() && z == claim.getMinZ()) {
                // Inferior direito: aponta -X e +Z
                goldLocA = new Location(world, x - 1, y, z);
                goldLocB = new Location(world, x, y, z + 1);
            } else if (x == claim.getMinX() && z == claim.getMaxZ()) {
                // Superior esquerdo: aponta +X e -Z
                goldLocA = new Location(world, x + 1, y, z);
                goldLocB = new Location(world, x, y, z - 1);
            } else if (x == claim.getMaxX() && z == claim.getMaxZ()) {
                // Superior direito: aponta -X e -Z
                goldLocA = new Location(world, x - 1, y, z);
                goldLocB = new Location(world, x, y, z - 1);
            } else {
                // fallback
                goldLocA = new Location(world, x + 1, y, z);
                goldLocB = new Location(world, x, y, z + 1);
            }

            Block realBlockGoldA = world.getBlockAt(goldLocA);
            originalBlocks.add(new OriginalBlockState(goldLocA, realBlockGoldA.getType(), realBlockGoldA.getData()));
            player.sendBlockChange(goldLocA, Material.GOLD_BLOCK, (byte) 0);

            Block realBlockGoldB = world.getBlockAt(goldLocB);
            originalBlocks.add(new OriginalBlockState(goldLocB, realBlockGoldB.getType(), realBlockGoldB.getData()));
            player.sendBlockChange(goldLocB, Material.GOLD_BLOCK, (byte) 0);
        }

        activeVisualizations.put(player.getName(), originalBlocks);
        currentlyVisualizing.put(player.getName(), claim.getOwnerName() + ";" + claim.getClaimName());
    }

    public void clearBorders(Player player) {
        String playerName = player.getName();
        Set<OriginalBlockState> originalBlocks = activeVisualizations.remove(playerName);

        if (originalBlocks != null) {
            for (OriginalBlockState state : originalBlocks) {
                player.sendBlockChange(state.getLocation(), state.getMaterial(), state.getData());
            }
        }
        currentlyVisualizing.remove(playerName);
    }
}
