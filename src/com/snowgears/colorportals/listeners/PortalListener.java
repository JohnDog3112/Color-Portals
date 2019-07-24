// 
// Decompiled by Procyon v0.5.36
// 

package com.snowgears.colorportals.listeners;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import com.snowgears.colorportals.ColorPortals;
import com.snowgears.colorportals.Portal;
import com.snowgears.colorportals.events.CreatePortalEvent;
import com.snowgears.colorportals.events.DestroyPortalEvent;
import com.snowgears.colorportals.utils.BukkitUtils;

public class PortalListener implements Listener
{
    public ColorPortals plugin;
    
    public PortalListener(final ColorPortals instance) {
        this.plugin = ColorPortals.getPlugin();
        this.plugin = instance;
    }
    
    @EventHandler
    public void onPortalCreate(final CreatePortalEvent event) {
        final Portal portal = event.getPortal();
        this.plugin.getPortalHandler().registerPortal(portal);
        event.getPlayer().sendMessage(ChatColor.GRAY + "You have added a " + portal.getColor().toString().toLowerCase() + " portal on channel " + portal.getChannel());
        this.plugin.getPortalHandler().savePortals();
    }
    
    @EventHandler
    public void onPortalDestroy(final DestroyPortalEvent event) {
        event.getPortal().remove();
        this.plugin.getPortalHandler().savePortals();
    }
    
    @EventHandler
    public void signDetachCheck(final BlockPhysicsEvent event) {
        final Block b = event.getBlock();
        if (BukkitUtils.isSign(b)) {
            final Portal portal = this.plugin.getPortalHandler().getPortal(b.getLocation());
            if (portal != null) {
                final BlockFace face = BukkitUtils.attatchedFace(((WallSign)b.getBlockData()).getFacing());
                if (!event.getBlock().getRelative(face).getType().isSolid()) {
                    if (this.plugin.getPortalProtection()) {
                        event.setCancelled(true);
                    }
                    else {
                        final DestroyPortalEvent e = new DestroyPortalEvent(portal, null);
                        this.plugin.getServer().getPluginManager().callEvent((Event)e);
                    }
                }
            }
        }
    }
    
    public boolean portalCanBeCreated(final Integer channel, final DyeColor color) {
        return this.plugin.getMaxPortalsPerGroup() == 0 || this.plugin.getPortalHandler().getPortalFamily(channel, color).size() < this.plugin.getMaxPortalsPerGroup();
    }
    
    public boolean frameIsComplete(final Location signLocation) {
        final WallSign sign = (WallSign)signLocation.getBlock().getBlockData();
        final Block keyBlock = signLocation.getBlock().getRelative(BukkitUtils.attatchedFace(sign.getFacing()));
        final DyeColor color = this.plugin.getBukkitUtils().getWoolColor(keyBlock);
        if (color == null) {
            return false;
        }
        BlockFace travel;
        if (sign.getFacing() == BlockFace.NORTH || sign.getFacing() == BlockFace.SOUTH) {
            travel = BlockFace.EAST;
        }
        else {
            travel = BlockFace.NORTH;
        }
        final Block topLeft = keyBlock.getRelative(travel);
        final Block topRight = keyBlock.getRelative(travel.getOppositeFace());
        final Block leftMid = topLeft.getRelative(BlockFace.DOWN);
        final Block rightMid = topRight.getRelative(BlockFace.DOWN);
        final Block lowerLeft = leftMid.getRelative(BlockFace.DOWN);
        final Block lowerRight = rightMid.getRelative(BlockFace.DOWN);
        final Block bottomLeft = lowerLeft.getRelative(BlockFace.DOWN);
        final Block bottomRight = lowerRight.getRelative(BlockFace.DOWN);
        final Block bottomMid = keyBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
        final Block[] array;
        final Block[] frameBlocks = array = new Block[] { keyBlock, topLeft, topRight, leftMid, rightMid, lowerLeft, lowerRight, bottomLeft, bottomRight, bottomMid };
        for (final Block block : array) {
            final DyeColor blockColor = this.plugin.getBukkitUtils().getWoolColor(block);
            if (blockColor != color) {
                return false;
            }
        }
        final Block buttonBlock = keyBlock.getRelative(BlockFace.DOWN);
        if (!buttonBlock.getType().name().contains("BUTTON")) {
            return false;
        }
        final Block plateBlock = bottomMid.getRelative(BlockFace.UP);
        return plateBlock.getType().name().contains("PRESSURE_PLATE");
    }
    
    public boolean checkPortalDistance(final Location currentLoc, final Player player, final int channel, final DyeColor color) {
        if (this.plugin.getMinDistance() != 0 || this.plugin.getMaxDistance() != 0) {
            if (this.plugin.getUsePerms() && player.hasPermission("colorportals.nodistance")) {
                return true;
            }
            final ArrayList<Portal> portals = this.plugin.getPortalHandler().getPortalFamily(channel, color);
            if (portals.size() != 0) {
                final Portal toConnect = portals.get(portals.size() - 1);
                final Location connectLoc = toConnect.getSignLocation();
                int distance;
                if (connectLoc.getWorld().equals(currentLoc.getWorld())) {
                    distance = (int)currentLoc.distance(connectLoc);
                }
                else {
                    distance = -1;
                }
                if (this.plugin.getMinDistance() != 0 && distance != -1 && distance < this.plugin.getMinDistance()) {
                    player.sendMessage(ChatColor.RED + "This portal is " + ChatColor.WHITE + (this.plugin.getMinDistance() - distance) + ChatColor.RED + " blocks too close to the previous portal in the chain.");
                    return false;
                }
                if (this.plugin.getMaxDistance() != 0) {
                    if (distance == -1) {
                        player.sendMessage(ChatColor.RED + "This portal cannot be created because there is a maximum distance of " + ChatColor.WHITE + this.plugin.getMaxDistance() + ChatColor.RED + " blocks and this portal is a different world than the previous portal in the chain.");
                        return false;
                    }
                    if (distance > this.plugin.getMaxDistance()) {
                        player.sendMessage(ChatColor.RED + "This portal is " + ChatColor.WHITE + (distance - this.plugin.getMaxDistance()) + ChatColor.RED + " blocks too far away from the previous portal in the chain.");
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
