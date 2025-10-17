package com.blockycraft.blockyclaim.listeners;

import com.blockycraft.blockyclaim.BlockyClaim;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

public class ExplosionListener extends EntityListener {

    private final BlockyClaim plugin;

    public ExplosionListener(BlockyClaim plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        
        // Verifica se a explosão foi causada por um Creeper
        if (entity instanceof Creeper) {
            Location explosionLocation = event.getLocation();
            
            // Se a localização da explosão estiver dentro de qualquer claim, cancela o evento.
            // Isso impede tanto o dano aos blocos quanto aos jogadores dentro da área.
            if (plugin.getClaimManager().getClaimAt(explosionLocation) != null) {
                event.setCancelled(true);
            }
        }
    }
}