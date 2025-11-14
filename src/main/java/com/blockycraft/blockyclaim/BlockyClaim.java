package com.blockycraft.blockyclaim;

import com.blockycraft.blockyclaim.commands.CommandManager;
import com.blockycraft.blockyclaim.config.ConfigManager;
import com.blockycraft.blockyclaim.database.DatabaseManager;
import com.blockycraft.blockyclaim.database.DatabaseManagerClaims;
import com.blockycraft.blockyclaim.geoip.GeoIPManager;
import com.blockycraft.blockyclaim.lang.LanguageManager;
import com.blockycraft.blockyclaim.listeners.*;
import com.blockycraft.blockyclaim.managers.ClaimManager;
import com.blockycraft.blockyclaim.managers.PlayerDataManager;
import com.blockycraft.blockyclaim.visualization.VisualizationManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class BlockyClaim extends JavaPlugin {
    private static BlockyClaim instance;
    private boolean factionsHookEnabled = false;
    private ConfigManager configManager;
    private ClaimManager claimManager;
    private PlayerDataManager playerDataManager;
    private VisualizationManager visualizationManager;
    private DatabaseManager databaseManager;
    private DatabaseManagerClaims databaseManagerClaims;
    private LanguageManager languageManager;
    private GeoIPManager geoIPManager;

    @Override
    public void onEnable() {
        instance = this;
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        this.geoIPManager = new GeoIPManager();
        this.claimManager = new ClaimManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.visualizationManager = new VisualizationManager(this);

        try {
            File dbFile = new File(getDataFolder(), "compras.db");
            databaseManager = new DatabaseManager(dbFile.getAbsolutePath());
            System.out.println("[BlockyClaim] Banco de dados de compras operacional.");
        } catch (Exception e) {
            System.out.println("[BlockyClaim] Erro ao iniciar banco de dados de compras!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            File claimsFile = new File(getDataFolder(), "claims.db");
            databaseManagerClaims = new DatabaseManagerClaims(claimsFile.getAbsolutePath());
            System.out.println("[BlockyClaim] Banco de dados de claims operacional.");
        } catch (Exception e) {
            System.out.println("[BlockyClaim] Erro ao iniciar banco de dados de claims!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.playerDataManager.loadPlayerData();
        this.claimManager.loadClaims();
        setupFactionsHook();
        registerListeners();
        registerCommands();

        this.visualizationManager.start();
        System.out.println("[BlockyClaim] Plugin ativado com sucesso!");
    }

    private void setupFactionsHook() {
        if (getServer().getPluginManager().isPluginEnabled("BlockyFactions")) {
            this.factionsHookEnabled = true;
            System.out.println("[BlockyClaim] Hook com BlockyFactions ativado com sucesso!");
        } else {
            System.out.println("[BlockyClaim] BlockyFactions nao encontrado. A integracao de faccoes esta desativada.");
        }
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
        if (databaseManager != null) {
            databaseManager.closeConnection();
            System.out.println("[BlockyClaim] Banco de dados de compras fechado.");
        }
        if (databaseManagerClaims != null) {
            databaseManagerClaims.closeConnection();
            System.out.println("[BlockyClaim] Banco de dados de claims fechado.");
        }
        System.out.println("[BlockyClaim] Plugin desativado.");
    }

    public static BlockyClaim getInstance() {
        return instance;
    }
    public boolean isFactionsHookEnabled() {
        return factionsHookEnabled;
    }
    public ConfigManager getConfigManager() { return configManager; }
    public ClaimManager getClaimManager() { return claimManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public VisualizationManager getVisualizationManager() { return visualizationManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public DatabaseManagerClaims getDatabaseManagerClaims() { return databaseManagerClaims; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public GeoIPManager getGeoIPManager() { return geoIPManager; }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);
        pm.registerEvents(new ClaimToolListener(this), this);
        pm.registerEvents(new ProtectionListener(this), this);
        pm.registerEvents(new InteractionListener(this), this);
        pm.registerEvents(new BoundaryListener(this), this);
        pm.registerEvents(new ExplosionListener(this), this);
        System.out.println("[BlockyClaim] Listeners de eventos registrados.");
    }

    private void registerCommands() {
        CommandManager commandManager = new CommandManager(this);
        getCommand("claim").setExecutor(commandManager);
        getCommand("trust").setExecutor(commandManager);
        getCommand("untrust").setExecutor(commandManager);
        System.out.println("[BlockyClaim] Comandos registrados.");
    }
}
