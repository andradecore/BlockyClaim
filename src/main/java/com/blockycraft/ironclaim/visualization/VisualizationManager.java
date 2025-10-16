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
            Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());

            boolean isHoldingTool = player.getItemInHand().getType() == claimTool;

            // CORREÇÃO: Estrutura mais explícita para a verificação de nulo
            if (isHoldingTool && claim != null) {
                // Dentro deste bloco, 'claim' NUNCA será nulo. O aviso desaparecerá.
                String claimId = claim.getOwnerName() + ";" + claim.getClaimName();
                if (!claimId.equals(currentlyVisualizing.get(player.getName()))) {
                    showBorders(player, claim);
                }
            } else {
                // Se o jogador não está segurando a ferramenta OU não está dentro de uma claim
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
        int y = player.getLocation().getBlockY() -1;

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