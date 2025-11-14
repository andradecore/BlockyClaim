package com.blockycraft.blockyclaim.listeners;

import com.blockycraft.blockyclaim.BlockyClaim;
import com.blockycraft.blockyclaim.config.ConfigManager;
import com.blockycraft.blockyclaim.lang.LanguageManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class ClaimToolListener implements Listener {

    private final BlockyClaim plugin;
    private final LanguageManager langManager;
    private static final Map<String, Location[]> pendingConfirmations = new HashMap<String, Location[]>();

    public ClaimToolListener(BlockyClaim plugin) {
        this.plugin = plugin;
        this.langManager = plugin.getLanguageManager();
    }
    
    public static Map<String, Location[]> getPendingConfirmations() {
        return pendingConfirmations;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ConfigManager cfg = plugin.getConfigManager();
        Material claimTool = cfg.getFerramentaClaim();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || player.getItemInHand().getType() != claimTool) {
            return;
        }
        event.setCancelled(true);
        Location clickedLocation = event.getClickedBlock().getLocation();
        String playerName = player.getName();
        String lang = plugin.getGeoIPManager().getPlayerLanguage(player);

        Map<String, Location[]> pendingConfirmations = ClaimToolListener.getPendingConfirmations();
        Location[] corners = pendingConfirmations.get(playerName);
        if (corners == null) {
            corners = new Location[2];
            pendingConfirmations.put(playerName, corners);
        }

        if (corners[0] == null) {
            corners[0] = clickedLocation;
            player.sendMessage(langManager.get(lang, "primeiro-canto"));
        } else {
            corners[1] = clickedLocation;

            if (plugin.getClaimManager().isAreaClaimed(corners[0], corners[1], playerName)) {
                player.sendMessage(langManager.get(lang, "selecao-sobrepoe"));
                pendingConfirmations.remove(playerName);
                return;
            }

            int claimSize = (Math.abs(corners[0].getBlockX() - corners[1].getBlockX()) + 1)
                    * (Math.abs(corners[0].getBlockZ() - corners[1].getBlockZ()) + 1);
            int minSize = cfg.getTamanhoMinimoClaim();
            if (minSize > 0 && claimSize < minSize) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("min_size", String.valueOf(minSize));
                player.sendMessage(langManager.get(lang, "tamanho-minimo-nao-atingido", placeholders));
                pendingConfirmations.remove(playerName);
                return;
            }

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("size", String.valueOf(claimSize));
            player.sendMessage(langManager.get(lang, "segundo-canto", placeholders));
            player.sendMessage(langManager.get(lang, "instrucao-confirmar"));
        }
    }
}