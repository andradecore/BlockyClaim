package com.blockycraft.blockyclaim.managers;

import com.blockycraft.blockyclaim.BlockyClaim;
import com.blockycraft.blockyclaim.data.Claim;
import org.bukkit.Location;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors; // Necessario para getClaimsByOwner

public class ClaimManager {
    
    private final BlockyClaim plugin;
    private List<Claim> claims;
    private File claimsFile;
    private Properties claimProps;

    public ClaimManager(BlockyClaim plugin) {
        this.plugin = plugin;
        this.claims = new ArrayList<Claim>();
        this.claimsFile = new File(plugin.getDataFolder(), "claims.properties");
        this.claimProps = new Properties();
    }

    /**
     * Verifica se um claim esta abandonado com base no ultimo login do dono.
     * @param claim O claim a ser verificado.
     * @return true se estiver abandonado, false caso contrario.
     */
    public boolean isAbandoned(Claim claim) {
        int horasParaAbandono = plugin.getConfigManager().getHorasParaAbandono();
        if (horasParaAbandono <= 0) {
            return false; // Abandono desativado
        }
        // Pega o ultimo login do dono (pode retornar 0 se nunca logou ou nao ha dados)
        long ultimoLogin = plugin.getPlayerDataManager().getLastLogin(claim.getOwnerName());
        if (ultimoLogin == 0) {
             // Se nao ha dados de login, nao podemos considerar abandonado
             // Poderia adicionar uma logica aqui baseada na data de criacao do claim, mas e complexo
            return false;
        }
        
        long tempoInativoMillis = System.currentTimeMillis() - ultimoLogin;
        long horasInativo = TimeUnit.MILLISECONDS.toHours(tempoInativoMillis); // Converte para horas
        
        return horasInativo >= horasParaAbandono;
    }

    /**
     * Retorna o claim na localizacao especificada, se houver.
     * @param location A localizacao.
     * @return O objeto Claim, ou null se nao houver claim.
     */
    public Claim getClaimAt(Location location) {
        for (Claim claim : claims) {
            if (claim.isLocationInside(location)) {
                return claim;
            }
        }
        return null;
    }

    /**
     * Retorna o claim mais proximo dentro de uma distancia especifica.
     * Usado pela visualizacao.
     * @param playerLoc Localizacao do jogador.
     * @param distance Distancia maxima.
     * @return O claim proximo, ou null.
     */
    public Claim getNearbyClaim(Location playerLoc, int distance) {
        String worldName = playerLoc.getWorld().getName();
        Claim closestClaim = null;
        double closestDistSq = Double.MAX_VALUE; // Compara distancia ao quadrado para eficiencia

        for (Claim claim : claims) {
            if (!claim.getWorldName().equals(worldName)) continue; // Ignora claims em outros mundos

            // Calcula a menor distancia do ponto (player) ao retangulo (claim) no plano XZ
            double dx = Math.max(claim.getMinX() - playerLoc.getX(), Math.max(0, playerLoc.getX() - (claim.getMaxX() + 1)));
            double dz = Math.max(claim.getMinZ() - playerLoc.getZ(), Math.max(0, playerLoc.getZ() - (claim.getMaxZ() + 1)));
            double distSq = dx * dx + dz * dz; // Distancia ao quadrado

            if (distSq <= distance * distance && distSq < closestDistSq) {
                 closestClaim = claim;
                 closestDistSq = distSq;
            }
        }
        return closestClaim;
    }

    /**
     * Retorna uma lista de todos os claims pertencentes a um jogador.
     * @param ownerName Nome do jogador (case-insensitive).
     * @return Lista de Claims (pode ser vazia).
     */
    public List<Claim> getClaimsByOwner(String ownerName) {
        // Usa a API de Stream do Java 8 para filtrar
        return claims.stream()
                     .filter(claim -> claim.getOwnerName().equalsIgnoreCase(ownerName))
                     .collect(Collectors.toList());
    }

    /**
     * NOVO METODO: Encontra um claim especifico pelo nome do dono e nome do claim.
     * @param ownerName Nome do dono (case-insensitive).
     * @param claimName Nome do claim (case-insensitive).
     * @return O objeto Claim, ou null se nao encontrado.
     */
    public Claim getClaimByName(String ownerName, String claimName) {
         if (ownerName == null || claimName == null) return null;
         for (Claim claim : claims) {
             if (claim.getOwnerName().equalsIgnoreCase(ownerName) && claim.getClaimName().equalsIgnoreCase(claimName)) {
                 return claim;
             }
         }
         return null;
    }

    /**
     * Verifica se uma area retangular sobrepoe algum claim existente.
     * @param pos1 Canto 1 da area.
     * @param pos2 Canto 2 da area.
     * @return true se houver sobreposicao, false caso contrario.
     */
    public boolean isAreaClaimed(Location pos1, Location pos2) {
        if (!pos1.getWorld().getName().equals(pos2.getWorld().getName())) return false; // Cantos em mundos diferentes? invalido.

        int selMinX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int selMaxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int selMinZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int selMaxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        String worldName = pos1.getWorld().getName();

        for (Claim existingClaim : claims) {
            // Verifica apenas claims no mesmo mundo
            if (existingClaim.getWorldName().equals(worldName)) {
                // Logica de colisao de retangulos
                boolean xOverlap = selMinX <= existingClaim.getMaxX() && selMaxX >= existingClaim.getMinX();
                boolean zOverlap = selMinZ <= existingClaim.getMaxZ() && selMaxZ >= existingClaim.getMinZ();
                if (xOverlap && zOverlap) {
                    return true; // Sobreposicao encontrada
                }
            }
        }
        return false; // Nenhuma sobreposicao
    }

    public void addClaim(Claim claim) { 
        if (claim != null) {
            claims.add(claim); 
        }
    }
    
    public void removeClaim(Claim claim) { 
        if (claim != null) {
            claims.remove(claim); 
        }
    }

    /**
     * Salva todos os claims para o arquivo claims.properties.
     */
    public void saveClaims() {
        // Garante que a pasta de dados exista
         if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        try (FileOutputStream fos = new FileOutputStream(claimsFile)) {
            claimProps.clear();
            int i = 0;
            for (Claim claim : claims) {
                // Serializa a lista de trusted players
                String trusted = String.join(",", claim.getTrustedPlayers());
                // Monta a string serializada
                String serialized = String.join(";",
                    claim.getOwnerName(),
                    claim.getClaimName(),
                    String.valueOf(claim.getCreationDate()),
                    claim.getWorldName(),
                    String.valueOf(claim.getMinX()), String.valueOf(claim.getMaxX()),
                    String.valueOf(claim.getMinZ()), String.valueOf(claim.getMaxZ()),
                    String.valueOf(claim.isForSale()),
                    String.valueOf(claim.getSalePrice()),
                    trusted // Adiciona a lista de trusted
                );
                claimProps.setProperty("claim." + i, serialized);
                i++;
            }
            claimProps.store(fos, "BlockyClaim Data - Claim Information");
            // System.out.println("[BlockyClaim] " + i + " claims salvos."); // Log menos verboso
        } catch (IOException e) {
            System.err.println("[BlockyClaim] ERRO: Nao foi possivel salvar claims.properties!");
            e.printStackTrace();
        }
    }

    /**
     * Carrega todos os claims do arquivo claims.properties.
     */
    public void loadClaims() {
        if (!claimsFile.exists()) return; // Nao ha nada para carregar
        
        try (FileInputStream fis = new FileInputStream(claimsFile)) {
            claimProps.load(fis);
            claims.clear(); // Limpa a lista antes de carregar

            for (String key : claimProps.stringPropertyNames()) {
                if (key.startsWith("claim.")) {
                    String s = claimProps.getProperty(key);
                    try {
                        String[] parts = s.split(";", -1); // Usa -1 para incluir campos vazios no final
                        if (parts.length < 11) { // Verifica numero minimo de partes
                            System.err.println("[BlockyClaim] Aviso: Formato invalido ao carregar claim (partes insuficientes): " + key + "=" + s);
                            continue;
                        }

                        String owner = parts[0];
                        String claimName = parts[1];
                        long creationDate = Long.parseLong(parts[2]);
                        String world = parts[3];
                        int minX = Integer.parseInt(parts[4]);
                        int maxX = Integer.parseInt(parts[5]);
                        int minZ = Integer.parseInt(parts[6]);
                        int maxZ = Integer.parseInt(parts[7]);
                        boolean forSale = Boolean.parseBoolean(parts[8]);
                        int salePrice = Integer.parseInt(parts[9]);
                        
                        // Deserializa a lista de trusted players
                        List<String> trusted = new ArrayList<>();
                        if (!parts[10].isEmpty()) { // Verifica se ha trusted players
                            trusted.addAll(Arrays.asList(parts[10].split(",")));
                        }
                        
                        // Adiciona o claim carregado a lista
                        claims.add(new Claim(owner, claimName, creationDate, world, minX, maxX, minZ, maxZ, forSale, salePrice, trusted));
                        
                    } catch (NumberFormatException e) {
                        System.err.println("[BlockyClaim] Aviso: Formato numerico invalido ao carregar claim: " + key + "=" + s);
                    } catch (ArrayIndexOutOfBoundsException e) {
                         System.err.println("[BlockyClaim] Aviso: Formato invalido ao carregar claim (ArrayIndex): " + key + "=" + s);
                    } catch (Exception e) { // Captura outras excecoes inesperadas
                        System.err.println("[BlockyClaim] ERRO inesperado ao carregar claim: " + key + "=" + s);
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("[BlockyClaim] " + claims.size() + " claims carregados.");
        } catch (IOException e) {
            System.err.println("[BlockyClaim] ERRO: Nao foi possivel carregar claims.properties!");
            e.printStackTrace();
        }
    } 
    
    /**
     * Retorna a lista completa de claims (usado pelo BlockyDynmap).
     * @return Lista de todos os claims.
     */
    public List<Claim> getAllClaims() {
        // Retorna uma copia para evitar modificacoes externas
        return new ArrayList<>(this.claims); 
    }
}