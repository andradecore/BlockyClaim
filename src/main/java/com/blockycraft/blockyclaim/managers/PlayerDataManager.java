package com.blockycraft.blockyclaim.managers;

import com.blockycraft.blockyclaim.BlockyClaim;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PlayerDataManager {
    
    private Map<String, Integer> claimBlocks;
    private Map<String, Long> lastLoginTimes; // <-- NOVO MAPA PARA ÚLTIMO LOGIN
    private File playerDataFile;
    private Properties playerProps;

    public PlayerDataManager(BlockyClaim plugin) {
        this.claimBlocks = new HashMap<String, Integer>();
        this.lastLoginTimes = new HashMap<String, Long>(); // <-- INICIALIZA O NOVO MAPA
        this.playerDataFile = new File(plugin.getDataFolder(), "playerdata.properties");
        this.playerProps = new Properties();
    }

    // --- NOVOS MÉTODOS PARA GERENCIAR O LOGIN ---
    public void updateLastLogin(String playerName) {
        lastLoginTimes.put(playerName.toLowerCase(), System.currentTimeMillis());
    }

    public long getLastLogin(String playerName) {
        Long lastLogin = lastLoginTimes.get(playerName.toLowerCase());
        return lastLogin != null ? lastLogin : 0;
    }
    // --- FIM DOS NOVOS MÉTODOS ---
    
    public boolean hasPlayerData(String playerName) {
        return claimBlocks.containsKey(playerName.toLowerCase());
    }

    public int getClaimBlocks(String playerName) {
        return claimBlocks.get(playerName.toLowerCase()) != null ? claimBlocks.get(playerName.toLowerCase()) : 0;
    }

    public void addClaimBlocks(String playerName, int amount) {
        int currentBlocks = getClaimBlocks(playerName);
        claimBlocks.put(playerName.toLowerCase(), currentBlocks + amount);
    }

    public boolean removeClaimBlocks(String playerName, int amount) {
        int currentBlocks = getClaimBlocks(playerName);
        if (currentBlocks < amount) {
            return false;
        }
        claimBlocks.put(playerName.toLowerCase(), currentBlocks - amount);
        return true;
    }
    
    public void savePlayerData() {
        try (FileOutputStream fos = new FileOutputStream(playerDataFile)) {
            playerProps.clear();
            // Salva os blocos de claim
            for (Map.Entry<String, Integer> entry : claimBlocks.entrySet()) {
                playerProps.setProperty("blocks." + entry.getKey(), entry.getValue().toString());
            }
            // Salva os timestamps de último login
            for (Map.Entry<String, Long> entry : lastLoginTimes.entrySet()) {
                playerProps.setProperty("login." + entry.getKey(), entry.getValue().toString());
            }
            playerProps.store(fos, "Player Data (Claim Blocks and Last Login)");
        } catch (IOException e) {
            System.out.println("[BlockyClaim] Erro ao salvar dados dos jogadores!");
            e.printStackTrace();
        }
    }

    public void loadPlayerData() {
        if (!playerDataFile.getParentFile().exists()) {
            playerDataFile.getParentFile().mkdirs();
        }
        if (!playerDataFile.exists()) {
            return;
        }

        try (FileInputStream fis = new FileInputStream(playerDataFile)) {
            playerProps.load(fis);
            for (String key : playerProps.stringPropertyNames()) {
                String playerName = key.substring(key.indexOf('.') + 1);
                try {
                    if (key.startsWith("blocks.")) {
                        int blocks = Integer.parseInt(playerProps.getProperty(key));
                        claimBlocks.put(playerName.toLowerCase(), blocks);
                    } else if (key.startsWith("login.")) {
                        long loginTime = Long.parseLong(playerProps.getProperty(key));
                        lastLoginTimes.put(playerName.toLowerCase(), loginTime);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[BlockyClaim] Erro ao carregar dados do jogador: " + playerName);
                }
            }
        } catch (IOException e) {
            System.out.println("[BlockyClaim] Erro ao carregar dados dos jogadores!");
            e.printStackTrace();
        }
    }
}