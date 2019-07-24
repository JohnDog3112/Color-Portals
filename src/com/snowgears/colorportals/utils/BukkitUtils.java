// 
// Decompiled by Procyon v0.5.36
// 

package com.snowgears.colorportals.utils;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class BukkitUtils
{
    public static HashMap<BlockFace, Integer> getCardinalDistances(final Location startLocation, final Location endLocation) {
        final HashMap<BlockFace, Integer> cardinalDistances = new HashMap<BlockFace, Integer>();
        final int northSouth = startLocation.getBlockZ() - endLocation.getBlockZ();
        if (northSouth >= 0) {
            cardinalDistances.put(BlockFace.NORTH, Math.abs(northSouth));
        }
        else {
            cardinalDistances.put(BlockFace.SOUTH, Math.abs(northSouth));
        }
        final int eastWest = startLocation.getBlockX() - endLocation.getBlockX();
        if (eastWest <= 0) {
            cardinalDistances.put(BlockFace.EAST, Math.abs(eastWest));
        }
        else {
            cardinalDistances.put(BlockFace.WEST, Math.abs(eastWest));
        }
        final int upDown = startLocation.getBlockY() - endLocation.getBlockY();
        if (upDown <= 0) {
            cardinalDistances.put(BlockFace.UP, Math.abs(upDown));
        }
        else {
            cardinalDistances.put(BlockFace.DOWN, Math.abs(upDown));
        }
        return cardinalDistances;
    }
    
    public byte determineDataOfDirection(final BlockFace bf) {
        if (bf.equals((Object)BlockFace.NORTH)) {
            return 2;
        }
        if (bf.equals((Object)BlockFace.SOUTH)) {
            return 5;
        }
        if (bf.equals((Object)BlockFace.WEST)) {
            return 3;
        }
        return (byte)(bf.equals((Object)BlockFace.EAST) ? 4 : 0);
    }
    
    public float faceToYaw(final BlockFace bf) {
        if (bf.equals((Object)BlockFace.NORTH)) {
            return 0.0f;
        }
        if (bf.equals((Object)BlockFace.EAST)) {
            return 90.0f;
        }
        if (bf.equals((Object)BlockFace.SOUTH)) {
            return 180.0f;
        }
        if (bf.equals((Object)BlockFace.WEST)) {
            return 270.0f;
        }
        return 0.0f;
    }
    
    public boolean isInteger(final String s) {
        try {
            Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    public DyeColor getWoolColor(final Block block) {
    	Material[] wools = {
    			Material.WHITE_WOOL,
    			Material.ORANGE_WOOL,
    			Material.MAGENTA_WOOL,
    			Material.LIGHT_BLUE_WOOL,
    			Material.YELLOW_WOOL,
    			Material.LIME_WOOL,
    			Material.PINK_WOOL,
    			Material.GRAY_WOOL,
    			Material.LIGHT_GRAY_WOOL,
    			Material.CYAN_WOOL,
    			Material.PURPLE_WOOL,
    			Material.BLUE_WOOL,
    			Material.BROWN_WOOL,
    			Material.GREEN_WOOL,
    			Material.RED_WOOL,
    			Material.BLACK_WOOL
    	};
    	DyeColor[] woolColors = {
    			DyeColor.WHITE,
    			DyeColor.ORANGE,
    			DyeColor.MAGENTA,
    			DyeColor.LIGHT_BLUE,
    			DyeColor.YELLOW,
    			DyeColor.LIME,
    			DyeColor.PINK,
    			DyeColor.GRAY,
    			DyeColor.LIGHT_GRAY,
    			DyeColor.CYAN,
    			DyeColor.PURPLE,
    			DyeColor.BLUE,
    			DyeColor.BROWN,
    			DyeColor.GREEN,
    			DyeColor.RED,
    			DyeColor.BLACK
    	};
    	int woolColor = -1;
    	for (int i = 0; i < wools.length; i++) {
    		if (wools[i] == block.getType()) {
    			woolColor = i;
    			break;
    		}
    	}
        if(woolColor == -1) return null;
        return woolColors[woolColor];
    }
    public static BlockFace attatchedFace(BlockFace f) {
		if (f == BlockFace.EAST) {
			return BlockFace.WEST;
		} else if (f== BlockFace.WEST) {
			return BlockFace.EAST;
		} else if (f== BlockFace.NORTH) {
			return BlockFace.SOUTH;
		} else if (f== BlockFace.SOUTH) {
			return BlockFace.NORTH;
		} else if (f == BlockFace.UP) {
			return BlockFace.DOWN;
		} else if (f== BlockFace.DOWN) {
			return BlockFace.UP;
		}
		return BlockFace.SELF;
	}
    public static boolean isSign(final Block block) {
    	Material[] Sign = {
			Material.ACACIA_WALL_SIGN,
			Material.BIRCH_WALL_SIGN,
			Material.DARK_OAK_WALL_SIGN,
			Material.JUNGLE_WALL_SIGN,
			Material.OAK_WALL_SIGN,
			Material.SPRUCE_WALL_SIGN
		};
    	for (int i = 0; i < Sign.length; i++) {
    		if (block.getType() == Sign[i]) {
    			return true;
    		}
    	}
    	return false;
    }
    public String getPlayerFromUUID(final UUID uid) {
        return null;
    }
}
