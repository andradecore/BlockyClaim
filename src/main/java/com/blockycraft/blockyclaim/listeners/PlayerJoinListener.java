package com.blockycraft.blockyclaim.listeners;

import com.blockycraft.blockyclaim.BlockyClaim;
import com.blockycraft.blockyclaim.managers.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final BlockyClaim plugin;

    public PlayerJoinListener(BlockyClaim plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        
        // Verifica se o jogador é novo (não tem dados de login salvos)
        boolean isNewPlayer = !playerDataManager.hasPlayerData(player.getName());

        // Atualiza a data do último login para o momento atual.
        // Isso acontece para jogadores novos e antigos.
        playerDataManager.updateLastLogin(player.getName());

        if (isNewPlayer) {
            int blocosIniciais = plugin.getConfigManager().getBlocosIniciais();
            if (blocosIniciais > 0) {
                playerDataManager.addClaimBlocks(player.getName(), blocosIniciais);
                System.out.println("[BlockyClaim] Deu " + blocosIniciais + " blocos de claim iniciais para o novo jogador " + player.getName());
            }
        }
    }
}