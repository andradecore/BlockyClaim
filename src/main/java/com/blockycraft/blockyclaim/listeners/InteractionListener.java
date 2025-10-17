package com.blockycraft.blockyclaim.listeners;

import com.blockycraft.blockyclaim.BlockyClaim;
import com.blockycraft.blockyclaim.config.ConfigManager;
import com.blockycraft.blockyclaim.data.Claim;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

public class InteractionListener extends PlayerListener {

    private final BlockyClaim plugin;

    public InteractionListener(BlockyClaim plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        
        // Ignora cliques no ar
        if (action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        Claim claim = plugin.getClaimManager().getClaimAt(clickedBlock.getLocation());

        // Se não houver claim no local, não há nada a fazer.
        if (claim == null) {
            return;
        }

        // Se o jogador não tem permissão na claim...
        if (!claim.hasPermission(player.getName())) {
            ConfigManager cfg = plugin.getConfigManager();
            Material type = clickedBlock.getType();

            // --- LÓGICA PARA CLIQUE DIREITO (USAR ITENS) ---
            if (action == Action.RIGHT_CLICK_BLOCK) {
                // Impede o uso da ferramenta de claim em terreno alheio
                if (player.getItemInHand().getType() == cfg.getFerramentaClaim()) {
                    event.setCancelled(true);
                    return;
                }

                // Lista de blocos interativos protegidos
                boolean isProtectedInteractable = type == Material.CHEST || type == Material.FURNACE || type == Material.BURNING_FURNACE ||
                                                  type == Material.WOODEN_DOOR || type == Material.IRON_DOOR_BLOCK || type == Material.LEVER ||
                                                  type == Material.STONE_BUTTON || type == Material.DISPENSER || type == Material.JUKEBOX;

                if (isProtectedInteractable) {
                    event.setCancelled(true);
                    player.sendMessage(cfg.getMsg("sem-permissao-interagir", "&cVoce nao pode interagir com blocos no terreno de &6{owner}&c.")
                        .replace("{owner}", claim.getOwnerName()));
                }
            }
            
            // --- NOVA LÓGICA PARA CLIQUE ESQUERDO (QUEBRAR BLOCOS FRÁGEIS) ---
            if (action == Action.LEFT_CLICK_BLOCK) {
                // Lista de blocos frágeis que podem ser quebrados com a mão
                boolean isFragile = type == Material.GRASS || type == Material.LONG_GRASS || type == Material.RED_ROSE || 
                                    type == Material.YELLOW_FLOWER || type == Material.SAPLING || type == Material.SNOW;

                if (isFragile) {
                    event.setCancelled(true);
                    // Usamos a mesma mensagem de "quebrar blocos" para consistência
                    player.sendMessage(cfg.getMsg("sem-permissao-quebrar", "&cVoce nao pode quebrar blocos no terreno de &6{owner}&c.")
                        .replace("{owner}", claim.getOwnerName()));
                }
            }
        }
    }
}