package com.blockycraft.blockyclaim.listeners;

import com.blockycraft.blockyclaim.BlockyClaim;
import com.blockycraft.blockyclaim.config.ConfigManager;
import com.blockycraft.blockyclaim.data.Claim;
import com.blockycraft.blockyclaim.lang.LanguageManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class InteractionListener implements Listener {

    private final BlockyClaim plugin;
    private final LanguageManager langManager;

    public InteractionListener(BlockyClaim plugin) {
        this.plugin = plugin;
        this.langManager = plugin.getLanguageManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        
        if (action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        Claim claim = plugin.getClaimManager().getClaimAt(clickedBlock.getLocation());

        if (claim == null) {
            return;
        }

        if (!claim.hasPermission(player.getName())) {
            ConfigManager cfg = plugin.getConfigManager();
            Material type = clickedBlock.getType();
            String lang = plugin.getGeoIPManager().getPlayerLanguage(player);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("owner", claim.getOwnerName());

            if (action == Action.RIGHT_CLICK_BLOCK) {
                if (player.getItemInHand().getType() == cfg.getFerramentaClaim()) {
                    event.setCancelled(true);
                    return;
                }

                boolean isProtectedInteractable = type == Material.CHEST || type == Material.FURNACE || type == Material.BURNING_FURNACE ||
                                                  type == Material.WOODEN_DOOR || type == Material.IRON_DOOR_BLOCK || type == Material.LEVER ||
                                                  type == Material.STONE_BUTTON || type == Material.DISPENSER || type == Material.JUKEBOX;

                if (isProtectedInteractable) {
                    event.setCancelled(true);
                    player.sendMessage(langManager.get(lang, "sem-permissao-interagir", placeholders));
                }
            }
            
            else if (action == Action.LEFT_CLICK_BLOCK) {
                boolean isFragile = type == Material.GRASS || type == Material.LONG_GRASS || type == Material.RED_ROSE || 
                                    type == Material.YELLOW_FLOWER || type == Material.SAPLING || type == Material.SNOW;
                
                boolean isLeftClickInteractable = type == Material.LEVER || type == Material.STONE_BUTTON;

                if (isFragile) {
                    event.setCancelled(true);
                    player.sendMessage(langManager.get(lang, "sem-permissao-quebrar", placeholders));
                } else if (isLeftClickInteractable) {
                    event.setCancelled(true);
                    player.sendMessage(langManager.get(lang, "sem-permissao-interagir", placeholders));
                }
            }
        }
    }
}