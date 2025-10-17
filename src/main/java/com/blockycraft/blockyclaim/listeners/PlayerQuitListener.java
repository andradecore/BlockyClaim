package com.blockycraft.blockyclaim.listeners;

import com.blockycraft.blockyclaim.BlockyClaim;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener extends PlayerListener {

    private final BlockyClaim plugin;

    public PlayerQuitListener(BlockyClaim plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Limpa os dados de visualização do jogador que está saindo para prevenir vazamento de memória.
        plugin.getVisualizationManager().clearBorders(player);
    }
}