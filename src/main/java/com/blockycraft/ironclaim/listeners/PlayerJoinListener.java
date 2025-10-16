package com.blockycraft.ironclaim.listeners;

import com.blockycraft.ironclaim.IronClaim;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

public class PlayerJoinListener extends PlayerListener {

    private final IronClaim plugin;

    public PlayerJoinListener(IronClaim plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Agora checamos se temos dados salvos para este jogador. Se não, ele é novo.
        if (!plugin.getPlayerDataManager().hasPlayerData(player.getName())) {
            int blocosIniciais = plugin.getConfigManager().getBlocosIniciais();
            if (blocosIniciais > 0) {
                plugin.getPlayerDataManager().addClaimBlocks(player.getName(), blocosIniciais);
                System.out.println("[IronClaim] Deu " + blocosIniciais + " blocos de claim iniciais para o novo jogador " + player.getName());
            }
        }
    }
}