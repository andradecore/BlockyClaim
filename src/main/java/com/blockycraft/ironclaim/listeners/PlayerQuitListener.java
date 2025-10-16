package com.blockycraft.ironclaim.listeners;

import com.blockycraft.ironclaim.IronClaim;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener extends PlayerListener {

    private final IronClaim plugin;

    public PlayerQuitListener(IronClaim plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Limpa os dados de visualização do jogador que está saindo para prevenir vazamento de memória.
        plugin.getVisualizationManager().clearBorders(player);
    }
}