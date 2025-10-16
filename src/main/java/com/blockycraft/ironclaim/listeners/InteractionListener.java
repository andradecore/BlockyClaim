package com.blockycraft.ironclaim.listeners;

import com.blockycraft.ironclaim.IronClaim;
import com.blockycraft.ironclaim.config.ConfigManager;
import com.blockycraft.ironclaim.data.Claim;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

public class InteractionListener extends PlayerListener {

    private final IronClaim plugin;

    public InteractionListener(IronClaim plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        ConfigManager cfg = plugin.getConfigManager();
        if (event.getPlayer().getItemInHand().getType() == cfg.getFerramentaClaim()) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        Material type = clickedBlock.getType();

        boolean isProtectedBlock = type == Material.CHEST || type == Material.FURNACE || type == Material.BURNING_FURNACE ||
                                   type == Material.WOODEN_DOOR || type == Material.IRON_DOOR_BLOCK || type == Material.LEVER ||
                                   type == Material.STONE_BUTTON || type == Material.DISPENSER || type == Material.JUKEBOX;

        if (isProtectedBlock) {
            Player player = event.getPlayer();
            Claim claim = plugin.getClaimManager().getClaimAt(clickedBlock.getLocation());

            if (claim != null && !claim.hasPermission(player.getName())) {
                event.setCancelled(true);
                player.sendMessage(cfg.getMsg("sem-permissao-interagir", "&cVoce nao pode interagir com blocos no terreno de &6{owner}&c.")
                    .replace("{owner}", claim.getOwnerName()));
            }
        }
    }
}