package com.blockycraft.ironclaim.managers; // Certifique-se que o pacote é 'managers'

import com.blockycraft.ironclaim.IronClaim;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PlayerDataManager {
    
    private Map<String, Integer> claimBlocks;
    private File playerDataFile;
    private Properties playerProps;

    public PlayerDataManager(IronClaim plugin) {
        this.claimBlocks = new HashMap<String, Integer>();
        this.playerDataFile = new File(plugin.getDataFolder(), "playerdata.properties");
        this.playerProps = new Properties();
    }
    
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
            for (Map.Entry<String, Integer> entry : claimBlocks.entrySet()) {
                playerProps.setProperty(entry.getKey(), entry.getValue().toString());
            }
            playerProps.store(fos, "Player Claim Blocks Data");
        } catch (IOException e) {
            System.out.println("[IronClaim] Erro ao salvar dados dos jogadores!");
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
            for (String playerName : playerProps.stringPropertyNames()) {
                try {
                    int blocks = Integer.parseInt(playerProps.getProperty(playerName));
                    claimBlocks.put(playerName.toLowerCase(), blocks);
                } catch (NumberFormatException e) {
                    System.out.println("[IronClaim] Erro ao carregar dados do jogador: " + playerName);
                }
            }
        } catch (IOException e) {
            System.out.println("[IronClaim] Erro ao carregar dados dos jogadores!");
            e.printStackTrace();
        }
    }
}