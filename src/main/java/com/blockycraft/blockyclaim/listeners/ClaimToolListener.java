package com.blockycraft.blockyclaim.listeners;

import com.blockycraft.blockyclaim.BlockyClaim;
import com.blockycraft.blockyclaim.config.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import java.util.HashMap;
import java.util.Map;

public class ClaimToolListener extends PlayerListener {

    private final BlockyClaim plugin;
    private static final Map<String, Location[]> pendingConfirmations = new HashMap<String, Location[]>();

    public ClaimToolListener(BlockyClaim plugin) {
        this.plugin = plugin;
    }
    
    public static Map<String, Location[]> getPendingConfirmations() {
        return pendingConfirmations;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    ConfigManager cfg = plugin.getConfigManager();
    Material claimTool = cfg.getFerramentaClaim();

    if (event.getAction() != Action.RIGHT_CLICK_BLOCK || player.getItemInHand().getType() != claimTool) {
        return;
    }
    event.setCancelled(true);
    Location clickedLocation = event.getClickedBlock().getLocation();
    String playerName = player.getName();

    Map<String, Location[]> pendingConfirmations = ClaimToolListener.getPendingConfirmations();
    Location[] corners = pendingConfirmations.get(playerName);
    if (corners == null) {
        corners = new Location[2];
        pendingConfirmations.put(playerName, corners);
    }

    if (corners[0] == null) {
        corners[0] = clickedLocation;
        player.sendMessage(cfg.getMsg("primeiro-canto", "&ePrimeiro canto definido! Clique no segundo canto."));
    } else {
        corners[1] = clickedLocation;

        // Corrigido: só barra claims de terceiros!
        if (plugin.getClaimManager().isAreaClaimed(corners[0], corners[1], playerName)) {
            player.sendMessage(cfg.getMsg("selecao-sobrepoe", "&cSua selecao sobrepoe um terreno ja protegido! Selecao cancelada."));
            pendingConfirmations.remove(playerName);
            return;
        }

        int claimSize = (Math.abs(corners[0].getBlockX() - corners[1].getBlockX()) + 1)
                * (Math.abs(corners[0].getBlockZ() - corners[1].getBlockZ()) + 1);
        int minSize = cfg.getTamanhoMinimoClaim();
        if (minSize > 0 && claimSize < minSize) {
            player.sendMessage(cfg.getMsg("tamanho-minimo-nao-atingido", "&cA sua selecao e muito pequena. O tamanho minimo e de &6{min_size} &cblocos.")
                    .replace("{min_size}", String.valueOf(minSize)));
            pendingConfirmations.remove(playerName);
            return;
        }

        player.sendMessage(cfg.getMsg("segundo-canto", "&eSegundo canto definido! A area tem &6{size} &eblocos.")
                .replace("{size}", String.valueOf(claimSize)));
        player.sendMessage(cfg.getMsg("instrucao-confirmar", "&aPara confirmar, digite: &b/claim confirm <nome>"));
    }
}
}