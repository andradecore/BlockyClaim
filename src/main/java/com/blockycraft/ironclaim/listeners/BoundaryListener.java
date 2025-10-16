package com.blockycraft.ironclaim.listeners;

import com.blockycraft.ironclaim.IronClaim;
import com.blockycraft.ironclaim.config.ConfigManager;
import com.blockycraft.ironclaim.data.Claim;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.HashMap;
import java.util.Map;

public class BoundaryListener extends PlayerListener {

    private final IronClaim plugin;
    private final Map<String, String> playerLastKnownClaimId = new HashMap<String, String>();

    public BoundaryListener(IronClaim plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!cfg.isAvisoFronteiraAtivado()) {
            return;
        }
        
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
                if (!claimAtLocation.hasPermission(player.getName())) {
                    player.sendMessage(cfg.getMsg("bem-vindo-claim", "&eVoce esta entrando em '&6{claim_name}&e' de &6{owner}&e.")
                        .replace("{claim_name}", claimAtLocation.getClaimName())
                        .replace("{owner}", claimAtLocation.getOwnerName()));
                }
                playerLastKnownClaimId.put(player.getName(), currentClaimId);
            }
        } 
        else if (lastClaimId != null) {
            playerLastKnownClaimId.remove(player.getName());
        }
    }
}