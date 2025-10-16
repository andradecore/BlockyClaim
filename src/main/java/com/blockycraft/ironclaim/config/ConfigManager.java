package com.blockycraft.ironclaim.config;

import com.blockycraft.ironclaim.IronClaim;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {

    private Properties props = new Properties();

    public ConfigManager(IronClaim plugin) {
        File configFile = new File(plugin.getDataFolder(), "config.properties");
        
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            // CORREÇÃO: Usa o ClassLoader para pegar o recurso de dentro do .jar
            try (InputStream in = IronClaim.class.getClassLoader().getResourceAsStream("config.properties");
                 FileOutputStream out = new FileOutputStream(configFile)) {
                
                if (in == null) {
                    System.out.println("[IronClaim] ERRO: config.properties nao encontrado no .jar!");
                    return;
                }
                
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getString(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    private int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(props.getProperty(key, String.valueOf(defaultValue)));
    }

    public Material getItemCompra() {
        try {
            return Material.valueOf(getString("economia.item-compra", "IRON_INGOT").toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.IRON_INGOT;
        }
    }
    
    public int getCustoPorBloco() { return getInt("economia.custo-por-bloco", 1); }
    public int getBlocosIniciais() { return getInt("economia.blocos-iniciais", 100); }
    
    public Material getFerramentaClaim() {
        try {
            return Material.valueOf(getString("regras.ferramenta-claim", "GOLD_SPADE").toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.GOLD_SPADE;
        }
    }

    public int getTamanhoMinimoClaim() { return getInt("regras.tamanho-minimo-claim", 100); }
    public int getMaxClaimsPorJogador() { return getInt("regras.max-claims-por-jogador", 5); }
    public boolean isAvisoFronteiraAtivado() { return getBoolean("funcionalidades.avisar-ao-entrar-na-claim", true); }

    public String getMsg(String path, String defaultValue) {
        String prefix = getString("mensagens.prefixo", "&6[IronClaim] &r");
        String message = getString("mensagens." + path, defaultValue);
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }
}