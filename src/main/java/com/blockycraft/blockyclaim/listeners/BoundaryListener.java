package com.blockycraft.blockyclaim.listeners;

import com.blockycraft.blockyclaim.BlockyClaim;
import com.blockycraft.blockyclaim.config.ConfigManager;
import com.blockycraft.blockyclaim.data.Claim;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.HashMap;
import java.util.Map;

public class BoundaryListener extends PlayerListener {

    private final BlockyClaim plugin;
    private final Map<String, String> playerLastKnownClaimId = new HashMap<String, String>();

    public BoundaryListener(BlockyClaim plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Claim claimAtLocation = plugin.getClaimManager().getClaimAt(to);
        String lastClaimId = playerLastKnownClaimId.get(player.getName());
        
        if (claimAtLocation != null) {
            String currentClaimId = claimAtLocation.getOwnerName() + ":" + claimAtLocation.getClaimName();
            if (!currentClaimId.equals(lastClaimId)) {
                ConfigManager cfg = plugin.getConfigManager();
                String ownerName = claimAtLocation.getOwnerName();
                String playerName = player.getName();
                
                // Prioridade 1: Notificar sobre claim abandonada
                if (plugin.getClaimManager().isAbandoned(claimAtLocation) && !playerName.equalsIgnoreCase(ownerName)) {
                    double custoOriginal = claimAtLocation.getSize() * cfg.getCustoPorBloco();
                    double percentualOcupar = cfg.getPercentualPrecoOcupar() / 100.0;
                    int custoFinal = (int) Math.ceil(custoOriginal * percentualOcupar);
                    String nomeItem = cfg.getItemCompra().name().replace("_", " ").toLowerCase();

                    player.sendMessage(cfg.getMsg("abandono.notificacao", "Este terreno esta abandonado!")
                        .replace("{claim_name}", claimAtLocation.getClaimName())
                        .replace("{cost}", String.valueOf(custoFinal))
                        .replace("{item_name}", nomeItem));
                
                // Prioridade 2: Notificar sobre claim à venda
                } else if (claimAtLocation.isForSale() && !playerName.equalsIgnoreCase(ownerName)) {
                    String nomeItem = cfg.getItemCompra().name().replace("_", " ").toLowerCase();
                    player.sendMessage(cfg.getMsg("venda.a-venda-notificacao", "Este terreno esta a venda!")
                        .replace("{claim_name}", claimAtLocation.getClaimName())
                        .replace("{price}", String.valueOf(claimAtLocation.getSalePrice()))
                        .replace("{item_name}", nomeItem));

                // Prioridade 3: Mensagem de boas-vindas normal
                } else if (cfg.isAvisoFronteiraAtivado()) {
                    if (!claimAtLocation.hasPermission(playerName)) {
                        player.sendMessage(cfg.getMsg("bem-vindo-claim", "&eVoce esta entrando em '&6{claim_name}&e' de &6{owner}&e.")
                            .replace("{claim_name}", claimAtLocation.getClaimName())
                            .replace("{owner}", ownerName));
                    }
                }
                playerLastKnownClaimId.put(player.getName(), currentClaimId);
            }
        } 
        else if (lastClaimId != null) {
            playerLastKnownClaimId.remove(player.getName());
        }
    }
}