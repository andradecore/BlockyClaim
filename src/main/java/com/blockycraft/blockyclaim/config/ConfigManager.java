package com.blockycraft.blockyclaim.config;

import com.blockycraft.blockyclaim.BlockyClaim;
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

    public ConfigManager(BlockyClaim plugin) {
        File configFile = new File(plugin.getDataFolder(), "config.properties");

        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try (InputStream in = BlockyClaim.class.getClassLoader().getResourceAsStream("config.properties");
                 FileOutputStream out = new FileOutputStream(configFile)) {

                if (in == null) {
                    System.out.println("[BlockyClaim] ERRO: config.properties nao encontrado no .jar!");
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

    private double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(props.getProperty(key, String.valueOf(defaultValue)));
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

    // ATUALIZADO: Agora retorna double para suportar custos fracionários
    public double getCustoPorBloco() {
        return getDouble("economia.custo-por-bloco", 1.0);
    }

    public int getBlocosIniciais() {
        return getInt("economia.blocos-iniciais", 100);
    }

    public Material getFerramentaClaim() {
        try {
            return Material.valueOf(getString("regras.ferramenta-claim", "GOLD_SPADE").toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.GOLD_SPADE;
        }
    }

    public int getTamanhoMinimoClaim() {
        return getInt("regras.tamanho-minimo-claim", 100);
    }

    public int getMaxClaimsPorJogador() {
        return getInt("regras.max-claims-por-jogador", 5);
    }

    public int getHorasParaAbandono() {
        return getInt("regras.horas-para-abandono", 24);
    }

    public int getPercentualPrecoOcupar() {
        return getInt("economia.percentual-preco-ocupar", 30);
    }

    public boolean isAvisoFronteiraAtivado() {
        return getBoolean("funcionalidades.avisar-ao-entrar-na-claim", true);
    }

    public String getMsg(String path, String defaultValue) {
        String prefix = getString("mensagens.prefixo", "");
        String message = getString("mensagens." + path, defaultValue);
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    public String getRawMsg(String path, String defaultValue) {
        String message = getString("mensagens." + path, defaultValue);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
