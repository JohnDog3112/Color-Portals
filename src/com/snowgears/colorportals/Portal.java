// 
// Decompiled by Procyon v0.5.36
// 

package com.snowgears.colorportals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.snowgears.colorportals.utils.BukkitUtils;

public class Portal implements Comparable<Portal>
{
    private Location signLocation;
    private Location warpLocation;
    private Collection<Location> occupiedLocations;
    private UUID creator;
    private String name;
    private int channel;
    private int node;
    private DyeColor color;
    private Portal linkedPortal;
    
    public Portal(final UUID creator, final String name, final DyeColor color, final int channel, final int node, final Location signLocation) {
        this.creator = creator;
        this.name = name;
        this.color = color;
        this.channel = channel;
        this.node = node;
        this.signLocation = signLocation;
        this.defineLocations();
    }
    
    public void updateSign() {
        ColorPortals.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask((Plugin)ColorPortals.getPlugin(), (Runnable)new Runnable() {
            @Override
            public void run() {
                if (BukkitUtils.isSign(Portal.this.signLocation.getBlock())) {
                    final Sign sign = (Sign)Portal.this.signLocation.getBlock().getState();
                    sign.setLine(0, Portal.this.name);
                    sign.setLine(1, Portal.this.channel + "." + Portal.this.node);
                    if (Portal.this.linkedPortal != null) {
                        sign.setLine(2, ChatColor.GREEN + "Warps To:");
                        sign.setLine(3, Portal.this.linkedPortal.getName());
                    }
                    else {
                        sign.setLine(2, "");
                        sign.setLine(3, ChatColor.GRAY + "INACTIVE");
                    }
                    sign.update(true);
                }
            }
        }, 2L);
    }
    
    public void remove() {
        ColorPortals.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask((Plugin)ColorPortals.getPlugin(), (Runnable)new Runnable() {
            @Override
            public void run() {
                if (BukkitUtils.isSign(Portal.this.signLocation.getBlock())) {
                    final Sign sign = (Sign)Portal.this.signLocation.getBlock().getState();
                    sign.setLine(0, ChatColor.RED + "PORTAL");
                    sign.setLine(1, ChatColor.RED + "DESTROYED");
                    sign.setLine(2, "");
                    sign.setLine(3, "");
                    sign.update(true);
                }
            }
        }, 2L);
        final ArrayList<Portal> portalFamily = ColorPortals.getPlugin().getPortalHandler().getPortalFamily(this);
        int beforeRemovedIndex = portalFamily.indexOf(this) - 1;
        if (beforeRemovedIndex < 0) {
            beforeRemovedIndex = portalFamily.size() - 1;
        }
        int afterRemovedIndex = portalFamily.indexOf(this) + 1;
        if (afterRemovedIndex >= portalFamily.size()) {
            afterRemovedIndex = 0;
        }
        final Portal beforeRemoved = portalFamily.get(beforeRemovedIndex);
        final Portal afterRemoved = portalFamily.get(afterRemovedIndex);
        if (beforeRemovedIndex == afterRemovedIndex) {
            if (this.getLinkedPortal() != null) {
                this.getLinkedPortal().setLinkedPortal(null);
            }
            ColorPortals.getPlugin().getPortalHandler().deregisterPortal(this);
            return;
        }
        beforeRemoved.setLinkedPortal(afterRemoved);
        for (int i = afterRemovedIndex; i < portalFamily.size(); ++i) {
            portalFamily.get(i).setNode(i);
        }
        ColorPortals.getPlugin().getPortalHandler().deregisterPortal(this);
    }
    
    public void teleport() {
        if (this.linkedPortal == null) {
            return;
        }
        for (final Entity e : this.warpLocation.getChunk().getEntities()) {
            if (e.getLocation().distanceSquared(this.getWarpLocation()) < 0.7 && ColorPortals.getPlugin().getEntityListener().entityCanBeTeleported(e)) {
                ColorPortals.getPlugin().getEntityListener().addNoTeleportEntity(e);
                e.teleport(this.linkedPortal.getWarpLocation());
            }
        }
        ColorPortals.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask((Plugin)ColorPortals.getPlugin(), (Runnable)new Runnable() {
            @Override
            public void run() {
                Portal.this.warpLocation.getWorld().playSound(Portal.this.warpLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
            }
        }, 2L);
        this.linkedPortal.getWarpLocation().getWorld().playSound(this.linkedPortal.getWarpLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
    }
    
    public Collection<Location> getOccupiedLocations() {
        return this.occupiedLocations;
    }
    
    public Collection<Block> getOccupiedBlocks() {
        final ArrayList<Block> occupiedBlocks = new ArrayList<Block>(this.occupiedLocations.size());
        for (final Location loc : this.occupiedLocations) {
            occupiedBlocks.add(loc.getBlock());
        }
        return occupiedBlocks;
    }
    
    public void printInfo(final Player player) {
        player.sendMessage(ChatColor.GOLD + "Portal: " + this.getName());
        player.sendMessage(ChatColor.GRAY + "   - Color: " + this.getColor().toString() + ", Channel: " + this.getChannel());
        player.sendMessage(ChatColor.GRAY + "   - Node: " + this.getNode() + " out of " + ColorPortals.getPlugin().getPortalHandler().getPortalFamily(this).size());
        final String creatorName = Bukkit.getOfflinePlayer(this.getCreator()).getName();
        player.sendMessage(ChatColor.GRAY + "   - Creator: " + creatorName);
        player.sendMessage(ChatColor.GREEN + "   - Warps To:");
        if (this.getLinkedPortal() == null) {
            player.sendMessage(ChatColor.GRAY + "      - No warp location set");
            return;
        }
        player.sendMessage(ChatColor.GRAY + "      - Name: " + this.getLinkedPortal().getName());
        if (this.getWarpLocation().getWorld().toString().equals(this.getLinkedPortal().getWarpLocation().getWorld().toString())) {
            final HashMap<BlockFace, Integer> cardinalDistances = BukkitUtils.getCardinalDistances(this.getWarpLocation(), this.getLinkedPortal().getWarpLocation());
            String cardinalMessage = "";
            for (final BlockFace direction : cardinalDistances.keySet()) {
                cardinalMessage = cardinalMessage + direction.toString() + ": " + cardinalDistances.get(direction) + " blocks, ";
            }
            cardinalMessage = cardinalMessage.substring(0, cardinalMessage.length() - 2);
            player.sendMessage(ChatColor.GRAY + "      - " + cardinalMessage);
        }
        else {
            player.sendMessage(ChatColor.GRAY + "      - Location: " + this.getLinkedPortal().getWarpLocation().getWorld().toString() + " (a different world)");
        }
        player.sendMessage(ChatColor.GRAY + "      - Biome: " + this.getLinkedPortal().getWarpLocation().getWorld().getBiome(this.getLinkedPortal().getWarpLocation().getBlockX(), this.getLinkedPortal().getWarpLocation().getBlockZ()).toString());
    }
    
    public UUID getCreator() {
        return this.creator;
    }
    
    public Location getSignLocation() {
        return this.signLocation;
    }
    
    public Location getWarpLocation() {
        return this.warpLocation;
    }
    
    public String getName() {
        return this.name;
    }
    
    public DyeColor getColor() {
        return this.color;
    }
    
    public int getChannel() {
        return this.channel;
    }
    
    public int getNode() {
        return this.node;
    }
    
    public void setNode(final int n) {
        this.node = n;
    }
    
    public Portal getLinkedPortal() {
        return this.linkedPortal;
    }
    
    public void setLinkedPortal(final Portal p) {
        this.linkedPortal = p;
        if (this.linkedPortal != null) {
            this.linkedPortal.updateSign();
        }
        else {
            this.node = 1;
        }
        this.updateSign();
    }
    
    @Override
    public int compareTo(final Portal other) {
        int i = other.color.compareTo((DyeColor)this.color);
        if (i != 0) {
            return i;
        }
        i = Integer.valueOf(this.channel).compareTo(other.channel);
        if (i != 0) {
            return i;
        }
        return Integer.valueOf(this.node).compareTo(other.node);
    }
    
    private void defineLocations() {
        final Block signBlock = this.signLocation.getBlock();
        final org.bukkit.block.data.type.WallSign sign = (org.bukkit.block.data.type.WallSign)signBlock.getBlockData();
        (this.warpLocation = signBlock.getRelative(BukkitUtils.attatchedFace(sign.getFacing())).getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getLocation()).add(0.5, 0.0, 0.5);
        this.warpLocation.setYaw(ColorPortals.getPlugin().getBukkitUtils().faceToYaw(sign.getFacing()) + 180.0f);
        BlockFace travel;
        if (sign.getFacing() == BlockFace.NORTH || sign.getFacing() == BlockFace.SOUTH) {
            travel = BlockFace.EAST;
        }
        else {
            travel = BlockFace.NORTH;
        }
        final Block midTop = signBlock.getRelative(BukkitUtils.attatchedFace(sign.getFacing()));
        final Block midUpper = midTop.getRelative(BlockFace.DOWN);
        final Block midLower = midTop.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
        final Block midBottom = midTop.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
        final Block leftTop = midTop.getRelative(travel);
        final Block leftUpper = midUpper.getRelative(travel);
        final Block leftLower = midLower.getRelative(travel);
        final Block leftBottom = midBottom.getRelative(travel);
        final Block rightTop = midTop.getRelative(travel.getOppositeFace());
        final Block rightUpper = midUpper.getRelative(travel.getOppositeFace());
        final Block rightLower = midLower.getRelative(travel.getOppositeFace());
        final Block rightBottom = midBottom.getRelative(travel.getOppositeFace());
        (this.occupiedLocations = new ArrayList<Location>()).add(this.signLocation);
        this.occupiedLocations.add(midTop.getLocation());
        this.occupiedLocations.add(midUpper.getLocation());
        this.occupiedLocations.add(midLower.getLocation());
        this.occupiedLocations.add(midBottom.getLocation());
        this.occupiedLocations.add(leftTop.getLocation());
        this.occupiedLocations.add(leftUpper.getLocation());
        this.occupiedLocations.add(leftLower.getLocation());
        this.occupiedLocations.add(leftBottom.getLocation());
        this.occupiedLocations.add(rightTop.getLocation());
        this.occupiedLocations.add(rightUpper.getLocation());
        this.occupiedLocations.add(rightLower.getLocation());
        this.occupiedLocations.add(rightBottom.getLocation());
    }
}
