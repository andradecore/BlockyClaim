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

    private final BlockyClaim plugin; // Campo e necessario para getConfiguration() e getDataFolder()
    private final Properties props = new Properties();
    private final File configFile; 

    public ConfigManager(BlockyClaim plugin) {
        this.plugin = plugin;
        // Usa this.plugin explicitamente para clareza e para potencialmente remover o aviso
        this.configFile = new File(this.plugin.getDataFolder(), "config.properties"); 
        
        // Garante que a pasta de dados exista
        if (!this.plugin.getDataFolder().exists()) {
            this.plugin.getDataFolder().mkdirs();
        }
        
        createDefaultConfigIfNotExists();
        loadProperties();
    }

    private void createDefaultConfigIfNotExists() {
        if (!configFile.exists()) {
            System.out.println("[BlockyClaim] Criando config.properties padrao...");
            // Usa getClass() para obter o ClassLoader relativo a esta classe
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties");
                 FileOutputStream out = new FileOutputStream(configFile)) {
                
                if (in == null) {
                    System.err.println("[BlockyClaim] ERRO FATAL: config.properties padrao nao encontrado no JAR!");
                    return;
                }
                
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            } catch (IOException e) {
                System.err.println("[BlockyClaim] ERRO: Nao foi possivel criar o config.properties padrao.");
                e.printStackTrace();
            }
        }
    }

    private void loadProperties() {
        // Garante que o arquivo exista antes de tentar ler (foi criado se necessario)
        if (!configFile.exists()) {
             System.err.println("[BlockyClaim] ERRO: config.properties nao encontrado apos tentativa de criacao.");
             return;
        }
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("[BlockyClaim] ERRO: Nao foi possivel carregar o config.properties.");
            e.printStackTrace();
        }
    }

    // --- Metodos de Leitura de Configuracao ---

    private String getString(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    private int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            System.err.println("[BlockyClaim] Aviso: Valor invalido para '" + key + "' no config.properties. Usando padrao: " + defaultValue);
            return defaultValue;
        } catch (NullPointerException e) { // Adiciona checagem para chave ausente
             System.err.println("[BlockyClaim] Aviso: Chave '" + key + "' nao encontrada no config.properties. Usando padrao: " + defaultValue);
             return defaultValue;
        }
    }
    
    private double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(props.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            System.err.println("[BlockyClaim] Aviso: Valor invalido para '" + key + "' no config.properties. Usando padrao: " + defaultValue);
            return defaultValue;
        } catch (NullPointerException e) {
             System.err.println("[BlockyClaim] Aviso: Chave '" + key + "' nao encontrada no config.properties. Usando padrao: " + defaultValue);
             return defaultValue;
        }
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        String value = props.getProperty(key); // Nao define valor padrao aqui para checar se existe
        if (value == null) {
             System.err.println("[BlockyClaim] Aviso: Chave '" + key + "' nao encontrada no config.properties. Usando padrao: " + defaultValue);
             return defaultValue;
        }
        // Permite "true" ou "false" (case insensitive)
        return Boolean.parseBoolean(value); 
    }

    // --- Getters Especificos ---

    public Material getItemCompra() {
        String materialName = getString("economia.item-compra", "IRON_INGOT");
        if (materialName == null || materialName.isEmpty()){
            System.err.println("[BlockyClaim] Aviso: 'economia.item-compra' esta vazio. Usando IRON_INGOT.");
            return Material.IRON_INGOT;
        }
        materialName = materialName.toUpperCase();
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            System.err.println("[BlockyClaim] Aviso: Material invalido para 'economia.item-compra': " + materialName + ". Usando IRON_INGOT.");
            return Material.IRON_INGOT;
        }
    }
    
    public double getCustoPorBloco() { 
        double cost = getDouble("economia.custo-por-bloco", 1.0); 
        if (cost <= 0) {
            System.err.println("[BlockyClaim] Aviso: 'economia.custo-por-bloco' deve ser maior que zero. Usando 1.0.");
            return 1.0;
        }
        return cost;
    }
    
    public int getBlocosIniciais() { return getInt("economia.blocos-iniciais", 0); } 
    
    public Material getFerramentaClaim() {
        String materialName = getString("regras.ferramenta-claim", "GOLD_SPADE");
         if (materialName == null || materialName.isEmpty()){
            System.err.println("[BlockyClaim] Aviso: 'regras.ferramenta-claim' esta vazio. Usando GOLD_SPADE.");
            return Material.GOLD_SPADE;
        }
        materialName = materialName.toUpperCase();
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
             System.err.println("[BlockyClaim] Aviso: Material invalido para 'regras.ferramenta-claim': " + materialName + ". Usando GOLD_SPADE.");
            return Material.GOLD_SPADE;
        }
    }

    public int getTamanhoMinimoClaim() { return getInt("regras.tamanho-minimo-claim", 10); } 
    public int getMaxClaimsPorJogador() { return getInt("regras.max-claims-por-jogador", 5); }
    
    public int getHorasParaAbandono() { return getInt("regras.horas-para-abandono", 2160); } 
    
    public int getPercentualPrecoOcupar() { return getInt("economia.percentual-preco-ocupar", 30); }

    public boolean isAvisoFronteiraAtivado() { return getBoolean("funcionalidades.avisar-ao-entrar-na-claim", true); }

    // --- Metodos de Mensagem ---

    public String getMsg(String path, String defaultValue) {
        String prefixMsg = getString("mensagens.prefixo", ""); 
        String message = getString("mensagens." + path, defaultValue); 
        String fullMessage = (prefixMsg != null && !prefixMsg.isEmpty()) ? prefixMsg + message : message;
        return ChatColor.translateAlternateColorCodes('&', fullMessage);
    }
    
    public String getRawMsg(String path, String defaultValue) {
        String message = getString("mensagens." + path, defaultValue);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}