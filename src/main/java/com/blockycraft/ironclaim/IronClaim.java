package com.blockycraft.ironclaim;

import com.blockycraft.ironclaim.commands.CommandManager;
import com.blockycraft.ironclaim.config.ConfigManager;
import com.blockycraft.ironclaim.listeners.*;
import com.blockycraft.ironclaim.managers.ClaimManager;
import com.blockycraft.ironclaim.managers.PlayerDataManager;
import com.blockycraft.ironclaim.visualization.VisualizationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class IronClaim extends JavaPlugin {

    private ConfigManager configManager;
    private ClaimManager claimManager;
    private PlayerDataManager playerDataManager;
    private VisualizationManager visualizationManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.claimManager = new ClaimManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.visualizationManager = new VisualizationManager(this);
        
        this.playerDataManager.loadPlayerData();
        this.claimManager.loadClaims();

        registerListeners();
        registerCommands();
        
        this.visualizationManager.start();

        System.out.println("[IronClaim] Plugin ativado com sucesso!");
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        
        pm.registerEvent(Type.PLAYER_JOIN, new PlayerJoinListener(this), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_QUIT, new PlayerQuitListener(this), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_INTERACT, new ClaimToolListener(this), Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, new ProtectionListener(this), Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_PLACE, new ProtectionListener(this), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_INTERACT, new InteractionListener(this), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_MOVE, new BoundaryListener(this), Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_EXPLODE, new ExplosionListener(this), Priority.High, this); // <-- Proteção contra Creeper

        System.out.println("[IronClaim] Listeners de eventos registrados.");
    }

    private void registerCommands() {
        CommandManager commandManager = new CommandManager(this);
        getCommand("claim").setExecutor(commandManager);
        getCommand("trust").setExecutor(commandManager);
        getCommand("untrust").setExecutor(commandManager);
        System.out.println("[IronClaim] Comandos registrados.");
    }

    @Override
    public void onDisable() {
        if (visualizationManager != null) {
            for (Player player : getServer().getOnlinePlayers()) {
                visualizationManager.clearBorders(player);
            }
        }
        
        this.playerDataManager.savePlayerData();
        this.claimManager.saveClaims();
        System.out.println("[IronClaim] Plugin desativado.");
    }

    // Getters para todos os managers
    public ConfigManager getConfigManager() { return configManager; }
    public ClaimManager getClaimManager() { return claimManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public VisualizationManager getVisualizationManager() { return visualizationManager; }
}