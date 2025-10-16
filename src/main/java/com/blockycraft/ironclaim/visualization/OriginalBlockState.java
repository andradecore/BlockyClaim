package com.blockycraft.ironclaim.visualization;

import org.bukkit.Location;
import org.bukkit.Material;

public class OriginalBlockState {
    private Location location;
    private Material material;
    private byte data;

    public OriginalBlockState(Location location, Material material, byte data) {
        this.location = location;
        this.material = material;
        this.data = data;
    }

    public Location getLocation() {
        return location;
    }

    public Material getMaterial() {
        return material;
    }

    public byte getData() {
        return data;
    }
}