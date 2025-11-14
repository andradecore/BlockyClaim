package com.blockycraft.blockyclaim.listeners;

import com.blockycraft.blockyclaim.BlockyClaim;
import com.blockycraft.blockyclaim.config.ConfigManager;
import com.blockycraft.blockyclaim.data.Claim;
import com.blockycraft.blockyclaim.lang.LanguageManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

public class BoundaryListener implements Listener {

    private final BlockyClaim plugin;
    private final LanguageManager langManager;
    private final Map<String, String> playerLastKnownClaimId = new HashMap<String, String>();

    public BoundaryListener(BlockyClaim plugin) {
        this.plugin = plugin;
        this.langManager = plugin.getLanguageManager();
    }

    @EventHandler
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
                String lang = plugin.getGeoIPManager().getPlayerLanguage(player);
                
                if (plugin.getClaimManager().isAbandoned(claimAtLocation) && !playerName.equalsIgnoreCase(ownerName)) {
                    double custoOriginal = claimAtLocation.getSize() * cfg.getCustoPorBloco();
                    double percentualOcupar = cfg.getPercentualPrecoOcupar() / 100.0;
                    int custoFinal = (int) Math.ceil(custoOriginal * percentualOcupar);
                    String nomeItem = cfg.getItemCompra().name().replace("_", " ").toLowerCase();

                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("claim_name", claimAtLocation.getClaimName());
                    placeholders.put("cost", String.valueOf(custoFinal));
                    placeholders.put("item_name", nomeItem);
                    player.sendMessage(langManager.get(lang, "abandono.notificacao", placeholders));
                
                } else if (claimAtLocation.isForSale() && !playerName.equalsIgnoreCase(ownerName)) {
                    String nomeItem = cfg.getItemCompra().name().replace("_", " ").toLowerCase();
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("claim_name", claimAtLocation.getClaimName());
                    placeholders.put("price", String.valueOf(claimAtLocation.getSalePrice()));
                    placeholders.put("item_name", nomeItem);
                    player.sendMessage(langManager.get(lang, "venda.a-venda-notificacao", placeholders));

                } else if (cfg.isAvisoFronteiraAtivado()) {
                    if (!claimAtLocation.hasPermission(playerName)) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("claim_name", claimAtLocation.getClaimName());
                        placeholders.put("owner", ownerName);
                        player.sendMessage(langManager.get(lang, "bem-vindo-claim", placeholders));
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