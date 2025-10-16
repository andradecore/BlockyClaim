package com.blockycraft.ironclaim.listeners;

import com.blockycraft.ironclaim.IronClaim;
import com.blockycraft.ironclaim.config.ConfigManager;
import com.blockycraft.ironclaim.data.Claim;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class ProtectionListener extends BlockListener {

    private final IronClaim plugin;

    public ProtectionListener(IronClaim plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        Claim claim = plugin.getClaimManager().getClaimAt(location);
        if (claim != null && !claim.hasPermission(player.getName())) {
            event.setCancelled(true);
            ConfigManager cfg = plugin.getConfigManager();
            player.sendMessage(cfg.getMsg("sem-permissao-quebrar", "&cVoce nao pode quebrar blocos no terreno de &6{owner}&c.")
                .replace("{owner}", claim.getOwnerName()));
        }
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        Claim claim = plugin.getClaimManager().getClaimAt(location);
        if (claim != null && !claim.hasPermission(player.getName())) {
            event.setCancelled(true);
            ConfigManager cfg = plugin.getConfigManager();
            player.sendMessage(cfg.getMsg("sem-permissao-construir", "&cVoce nao pode construir no terreno de &6{owner}&c.")
                .replace("{owner}", claim.getOwnerName()));
        }
    }
}