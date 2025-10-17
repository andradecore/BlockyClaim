package com.blockycraft.blockyclaim.data;

import org.bukkit.Location;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Claim {

    private String ownerName;
    private String claimName;
    private long creationDate;
    private String worldName;
    private int minX, maxX, minZ, maxZ;
    private List<String> trustedPlayers;
    private boolean forSale;
    private int salePrice;

    public Claim(String ownerName, String claimName, Location pos1, Location pos2) {
        this.ownerName = ownerName;
        this.claimName = claimName;
        this.creationDate = System.currentTimeMillis();
        this.worldName = pos1.getWorld().getName();
        this.minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        this.trustedPlayers = new ArrayList<String>();
        this.forSale = false;
        this.salePrice = 0;
    }

    public Claim(String ownerName, String claimName, long creationDate, String worldName, int minX, int maxX, int minZ, int maxZ, boolean forSale, int salePrice, List<String> trusted) {
        this.ownerName = ownerName;
        this.claimName = claimName;
        this.creationDate = creationDate;
        this.worldName = worldName;
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.forSale = forSale;
        this.salePrice = salePrice;
        this.trustedPlayers = trusted;
    }
    
    public String getOwnerName() { return ownerName; }
    public String getClaimName() { return claimName; }
    public String getFormattedCreationDate() { return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(creationDate)); }
    public long getCreationDate() { return creationDate; }
    public String getWorldName() { return worldName; }
    public int getMinX() { return minX; }
    public int getMaxX() { return maxX; }
    public int getMinZ() { return minZ; }
    public int getMaxZ() { return maxZ; }
    public List<String> getTrustedPlayers() { return trustedPlayers; }
    public int getSize() { return (maxX - minX + 1) * (maxZ - minZ + 1); }

    public boolean isForSale() { return forSale; }
    public int getSalePrice() { return salePrice; }

    public void putForSale(int price) {
        this.forSale = true;
        this.salePrice = price;
    }

    public void removeFromSale() {
        this.forSale = false;
        this.salePrice = 0;
    }
    
    public void setClaimName(String newName) {
        this.claimName = newName;
    }

    public boolean isLocationInside(Location location) {
        if (!location.getWorld().getName().equals(worldName)) {
            return false;
        }
        int x = location.getBlockX();
        int z = location.getBlockZ();
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public boolean hasPermission(String playerName) {
        return ownerName.equalsIgnoreCase(playerName) || trustedPlayers.contains(playerName.toLowerCase());
    }

    public void trustPlayer(String playerName) {
        if (!trustedPlayers.contains(playerName.toLowerCase())) {
            trustedPlayers.add(playerName.toLowerCase());
        }
    }

    public void untrustPlayer(String playerName) {
        trustedPlayers.remove(playerName.toLowerCase());
    }

    public void setOwner(String newOwnerName) {
        this.ownerName = newOwnerName;
    }
}