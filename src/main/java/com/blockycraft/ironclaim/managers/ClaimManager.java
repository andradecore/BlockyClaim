package com.blockycraft.ironclaim.managers;

import com.blockycraft.ironclaim.IronClaim;
import com.blockycraft.ironclaim.data.Claim;
import org.bukkit.Location;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ClaimManager {

    private List<Claim> claims;
    private File claimsFile;
    private Properties claimProps;

    public ClaimManager(IronClaim plugin) {
        this.claims = new ArrayList<Claim>();
        this.claimsFile = new File(plugin.getDataFolder(), "claims.properties");
        this.claimProps = new Properties();
    }

    public Claim getClaimAt(Location location) {
        for (Claim claim : claims) {
            if (claim.isLocationInside(location)) {
                return claim;
            }
        }
        return null;
    }

    // NOVO MÉTODO: Retorna a primeira claim próxima à localização, dentro do raio (ex: 10 blocos)
    public Claim getNearbyClaim(Location playerLoc, int distance) {
        String worldName = playerLoc.getWorld().getName();
        for (Claim claim : claims) {
            if (!claim.getWorldName().equals(worldName)) continue; // Mundial correto
            int dx = Math.max(claim.getMinX() - playerLoc.getBlockX(), playerLoc.getBlockX() - claim.getMaxX());
            int dz = Math.max(claim.getMinZ() - playerLoc.getBlockZ(), playerLoc.getBlockZ() - claim.getMaxZ());
            dx = Math.max(dx, 0);
            dz = Math.max(dz, 0);
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist <= distance) {
                return claim;
            }
        }
        return null;
    }

    public List<Claim> getClaimsByOwner(String ownerName) {
        List<Claim> ownerClaims = new ArrayList<Claim>();
        for (Claim claim : claims) {
            if (claim.getOwnerName().equalsIgnoreCase(ownerName)) {
                ownerClaims.add(claim);
            }
        }
        return ownerClaims;
    }

    public boolean isAreaClaimed(Location pos1, Location pos2) {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (Claim existingClaim : claims) {
            if (existingClaim.getWorldName().equals(pos1.getWorld().getName())) {
                if (minX <= existingClaim.getMaxX() && maxX >= existingClaim.getMinX() &&
                    minZ <= existingClaim.getMaxZ() && maxZ >= existingClaim.getMinZ()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addClaim(Claim claim) { claims.add(claim); }
    public void removeClaim(Claim claim) { claims.remove(claim); }

    public void saveClaims() {
        try (FileOutputStream fos = new FileOutputStream(claimsFile)) {
            claimProps.clear();
            int i = 0;
            for (Claim claim : claims) {
                String trusted = String.join(",", claim.getTrustedPlayers());
                String serialized = claim.getOwnerName() + ";" +
                                    claim.getClaimName() + ";" +
                                    claim.getCreationDate() + ";" +
                                    claim.getWorldName() + ";" +
                                    claim.getMinX() + ";" + claim.getMaxX() + ";" +
                                    claim.getMinZ() + ";" + claim.getMaxZ() + ";" +
                                    trusted;
                claimProps.setProperty("claim." + i, serialized);
                i++;
            }
            claimProps.store(fos, "Claim Data");
        } catch (IOException e) {
            System.out.println("[IronClaim] Erro ao salvar claims!");
            e.printStackTrace();
        }
    }

    public void loadClaims() {
        if (!claimsFile.exists()) return;

        try (FileInputStream fis = new FileInputStream(claimsFile)) {
            claimProps.load(fis);
            for (String key : claimProps.stringPropertyNames()) {
                if (key.startsWith("claim.")) {
                    String s = claimProps.getProperty(key);
                    try {
                        String[] parts = s.split(";", -1);
                        String owner = parts[0];
                        String claimName = parts[1];
                        long creationDate = Long.parseLong(parts[2]);
                        String world = parts[3];
                        int minX = Integer.parseInt(parts[4]);
                        int maxX = Integer.parseInt(parts[5]);
                        int minZ = Integer.parseInt(parts[6]);
                        int maxZ = Integer.parseInt(parts[7]);
                        List<String> trusted = new ArrayList<String>();
                        if (parts.length > 8 && !parts[8].isEmpty()) {
                            trusted.addAll(Arrays.asList(parts[8].split(",")));
                        }
                        claims.add(new Claim(owner, claimName, creationDate, world, minX, maxX, minZ, maxZ, trusted));
                    } catch (Exception e) {
                        System.out.println("[IronClaim] Erro ao carregar claim (formato invalido): " + s);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("[IronClaim] Erro ao carregar claims!");
            e.printStackTrace();
        }
    }
}
