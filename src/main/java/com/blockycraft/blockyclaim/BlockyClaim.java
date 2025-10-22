package com.blockycraft.blockyclaim;

import com.blockycraft.blockyclaim.commands.CommandManager;
import com.blockycraft.blockyclaim.config.ConfigManager;
import com.blockycraft.blockyclaim.listeners.*;
import com.blockycraft.blockyclaim.managers.ClaimManager;
import com.blockycraft.blockyclaim.managers.PlayerDataManager;
import com.blockycraft.blockyclaim.visualization.VisualizationManager;
import com.blockycraft.blockywar.api.BlockyWarAPI; // <-- NOVO IMPORT
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockyClaim extends JavaPlugin {

    private static BlockyClaim instance;
    private boolean factionsHookEnabled = false;
    private boolean warHookEnabled = false; // <-- NOVA VARIAVEL

    private ConfigManager configManager;
    private ClaimManager claimManager;
    private PlayerDataManager playerDataManager;
    private VisualizationManager visualizationManager;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.claimManager = new ClaimManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.visualizationManager = new VisualizationManager(this);
        
        this.playerDataManager.loadPlayerData();
        this.claimManager.loadClaims();

        setupFactionsHook();
        setupWarHook(); // <-- NOVA CHAMADA

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

    /**
     * NOVO METODO: Verifica a presenca do BlockyWar e inicializa a API.
     */
    private void setupWarHook() {
        if (getServer().getPluginManager().isPluginEnabled("BlockyWar")) {
            try {
                // Tenta inicializar a API do BlockyWar
                // A classe BlockyWarAPI sera carregada automaticamente se o plugin estiver presente
                BlockyWarAPI.initialize(null); // Passamos null aqui pois a API e estatica
                this.warHookEnabled = true;
                System.out.println("[BlockyClaim] Hook com BlockyWar ativado com sucesso!");
            } catch (NoClassDefFoundError e) {
                // Isso pode acontecer se o JAR do BlockyWar nao contiver a API
                System.out.println("[BlockyClaim] ERRO: BlockyWar encontrado, mas a BlockyWarAPI nao pode ser carregada.");
                this.warHookEnabled = false;
            }
        } else {
            System.out.println("[BlockyClaim] BlockyWar nao encontrado. A integracao de guerra esta desativada.");
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
        System.out.println("[BlockyClaim] Plugin desativado.");
    }

    // --- Getters ---
    public static BlockyClaim getInstance() {
        return instance;
    }

    public boolean isFactionsHookEnabled() {
        return factionsHookEnabled;
    }

    /**
     * NOVO GETTER: Verifica se a integracao com BlockyWar esta ativa.
     * @return true se o hook com BlockyWar estiver ativo.
     */
    public boolean isWarHookEnabled() { // <-- NOVO GETTER
        return warHookEnabled;
    }

    public ConfigManager getConfigManager() { return configManager; }
    public ClaimManager getClaimManager() { return claimManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public VisualizationManager getVisualizationManager() { return visualizationManager; }
    
    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        
        pm.registerEvent(Type.PLAYER_JOIN, new PlayerJoinListener(this), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_QUIT, new PlayerQuitListener(this), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_INTERACT, new ClaimToolListener(this), Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, new ProtectionListener(this), Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_PLACE, new ProtectionListener(this), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_INTERACT, new InteractionListener(this), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_MOVE, new BoundaryListener(this), Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_EXPLODE, new ExplosionListener(this), Priority.High, this);

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