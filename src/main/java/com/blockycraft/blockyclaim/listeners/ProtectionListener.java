package com.blockycraft.blockyclaim.listeners;

import com.blockycraft.blockyclaim.BlockyClaim;
import com.blockycraft.blockyclaim.data.Claim;
import com.blockycraft.blockyclaim.lang.LanguageManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.Map;

public class ProtectionListener implements Listener {

    private final BlockyClaim plugin;
    private final LanguageManager langManager;

    public ProtectionListener(BlockyClaim plugin) {
        this.plugin = plugin;
        this.langManager = plugin.getLanguageManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        Claim claim = plugin.getClaimManager().getClaimAt(location);
        if (claim != null && !claim.hasPermission(player.getName())) {
            event.setCancelled(true);
            String lang = plugin.getGeoIPManager().getPlayerLanguage(player);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("owner", claim.getOwnerName());
            player.sendMessage(langManager.get(lang, "sem-permissao-quebrar", placeholders));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        Claim claim = plugin.getClaimManager().getClaimAt(location);
        if (claim != null && !claim.hasPermission(player.getName())) {
            event.setCancelled(true);
            String lang = plugin.getGeoIPManager().getPlayerLanguage(player);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("owner", claim.getOwnerName());
            player.sendMessage(langManager.get(lang, "sem-permissao-construir", placeholders));
        }
    }
}