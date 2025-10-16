package com.blockycraft.ironclaim.visualization;

import com.blockycraft.ironclaim.IronClaim;
import com.blockycraft.ironclaim.data.Claim;
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

    private final IronClaim plugin;
    private final Map<String, Set<OriginalBlockState>> activeVisualizations = new HashMap<>();
    private final Map<String, String> currentlyVisualizing = new HashMap<>();

    public VisualizationManager(IronClaim plugin) {
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
            Claim claim = plugin.getClaimManager().getNearbyClaim(player.getLocation(), 10); // Nova lógica

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
        List<Location> cornerLocations = new ArrayList<>();
        int y = player.getLocation().getBlockY() - 1; // 1 bloco abaixo

        cornerLocations.add(new Location(world, claim.getMinX(), y, claim.getMinZ()));
        cornerLocations.add(new Location(world, claim.getMaxX(), y, claim.getMinZ()));
        cornerLocations.add(new Location(world, claim.getMinX(), y, claim.getMaxZ()));
        cornerLocations.add(new Location(world, claim.getMaxX(), y, claim.getMaxZ()));

        Set<OriginalBlockState> originalBlocks = new HashSet<>();

        for (Location loc : cornerLocations) {
            Block realBlock = world.getBlockAt(loc);
            originalBlocks.add(new OriginalBlockState(loc, realBlock.getType(), realBlock.getData()));
            player.sendBlockChange(loc, Material.GLOWSTONE, (byte) 0);
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
