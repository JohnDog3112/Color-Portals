// 
// Decompiled by Procyon v0.5.36
// 

package com.snowgears.colorportals.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.snowgears.colorportals.ColorPortals;
import com.snowgears.colorportals.Portal;
import com.snowgears.colorportals.events.CreatePortalEvent;
import com.snowgears.colorportals.events.DestroyPortalEvent;
import com.snowgears.colorportals.utils.BukkitUtils;

public class EntityListener implements Listener
{
    public ColorPortals plugin;
    private HashMap<UUID, Boolean> noTeleportEntities;
    
    public EntityListener(final ColorPortals instance) {
        this.plugin = ColorPortals.getPlugin();
        this.noTeleportEntities = new HashMap<UUID, Boolean>();
        this.plugin = instance;
    }
    
    @EventHandler
    public void onSignWrite(final SignChangeEvent event) {
        if (BukkitUtils.isSign(event.getBlock())) {
            final WallSign s = (WallSign)event.getBlock().getBlockData();
            final Block attachedBlock = event.getBlock().getRelative(BukkitUtils.attatchedFace(s.getFacing()));
            if (attachedBlock.getType().name().contains("WOOL") && attachedBlock.getLocation().clone().add(0.0, -3.0, 0.0).getBlock().getType().name().contains("WOOL")) {
                if (!this.plugin.getPortalListener().frameIsComplete(event.getBlock().getLocation())) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Your portal's frame is either not complete or it is missing the button and/or pressure plate.");
                    return;
                }
                if (this.plugin.getUsePerms() && !event.getPlayer().hasPermission("colorportals.create")) {
                    event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are not authorized to create portals");
                    event.setCancelled(true);
                    return;
                }
                final Portal p = this.plugin.getPortalHandler().getPortal(attachedBlock.getRelative(BukkitUtils.attatchedFace(s.getFacing())).getLocation());
                if (p != null) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You can not put another sign on the top of this portal.");
                    event.setCancelled(true);
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(event.getBlock().getType()));
                }
                final String name = event.getLine(0);
                if (name.length() == 0) {
                    event.getPlayer().sendMessage(ChatColor.RED + "The name of the portal cannot be left blank");
                    return;
                }
                final String chan = event.getLine(1);
                if (!this.plugin.getBukkitUtils().isInteger(chan)) {
                    event.getPlayer().sendMessage(ChatColor.RED + "The channel must be an integer in order to create a portal");
                    return;
                }
                final int channel = Integer.parseInt(chan);
                if (channel < 0 || channel > 9999) {
                    event.getPlayer().sendMessage(ChatColor.RED + "The channel must be between 0 and 10,000.");
                    return;
                }
                final DyeColor color = this.plugin.getBukkitUtils().getWoolColor(attachedBlock);
                if (!this.plugin.getPortalListener().checkPortalDistance(attachedBlock.getLocation(), event.getPlayer(), channel, color)) {
                    event.setCancelled(true);
                    return;
                }
                if (this.plugin.getPortalListener().portalCanBeCreated(channel, color)) {
                    final int node = this.plugin.getPortalHandler().getPortalFamily(channel, color).size() + 1;
                    final Portal portal = new Portal(event.getPlayer().getUniqueId(), name, color, channel, node, event.getBlock().getLocation());
                    final CreatePortalEvent e = new CreatePortalEvent(portal, event.getPlayer());
                    Bukkit.getServer().getPluginManager().callEvent((Event)e);
                }
                else {
                    event.getPlayer().sendMessage(ChatColor.RED + "There are already " + this.plugin.getMaxPortalsPerGroup() + " " + color + " portals on channel " + channel + ".");
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerBlockBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        Portal portal = null;
        final Material blockType = event.getBlock().getType();
        if (BukkitUtils.isSign(event.getBlock())) {
            portal = this.plugin.getPortalHandler().getPortal(event.getBlock().getLocation());
        }
        else if (blockType.name().contains("WOOL")) {
            portal = this.plugin.getPortalHandler().getPortalByFrameLocation(event.getBlock().getLocation());
        }
        else if (blockType.name().contains("BUTTON") || blockType.name().contains("PRESSURE_PLATE")) {
            portal = this.plugin.getPortalHandler().getPortalByFrameLocation(event.getBlock().getLocation());
        }
        if (portal != null) {
            if (this.plugin.getUsePerms() && !event.getPlayer().hasPermission("colorportals.destroy")) {
                player.sendMessage(ChatColor.DARK_RED + "You are not authorized to destroy portals");
                event.setCancelled(true);
                return;
            }
            if (this.plugin.getPortalProtection()) {
                if (portal.getCreator().equals(player.getUniqueId()) || player.isOp()) {
                    final DestroyPortalEvent e = new DestroyPortalEvent(portal, (Entity)event.getPlayer());
                    Bukkit.getServer().getPluginManager().callEvent((Event)e);
                }
                else {
                    player.sendMessage(ChatColor.DARK_RED + "You are not authorized to destroy this portal");
                    event.setCancelled(true);
                }
            }
            else {
                final DestroyPortalEvent e = new DestroyPortalEvent(portal, (Entity)event.getPlayer());
                Bukkit.getServer().getPluginManager().callEvent((Event)e);
            }
        }
    }
    
    @EventHandler
    public void onPlayerButtonPress(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!event.getClickedBlock().getType().name().contains("BUTTON")) {
            return;
        }
        final Block portalKeyBlock = event.getClickedBlock().getRelative(BlockFace.UP);
        final Portal portal = this.plugin.getPortalHandler().getPortalByKeyBlock(portalKeyBlock);
        if (portal == null) {
            return;
        }
        if (portal.getLinkedPortal() == null) {
            return;
        }
        if (!this.plugin.getUsePerms() || player.hasPermission("colorportals.use")) {
            portal.teleport();
        }
        else {
            player.sendMessage(ChatColor.DARK_RED + "You are not authorized to use portals");
        }
    }
    
    @EventHandler
    public void onPlayerSignClick(final PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            final Portal portal = this.plugin.getPortalHandler().getPortal(event.getClickedBlock().getLocation());
            if (portal != null) {
                portal.printInfo(event.getPlayer());
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerActivatePressurePlate(final PlayerInteractEvent event) {
        if (!this.plugin.getWalkOnActivation()) {
            return;
        }
        final Player player = event.getPlayer();
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }
        final Block portalKeyBlock = event.getClickedBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP);
        final Portal portal = this.plugin.getPortalHandler().getPortalByKeyBlock(portalKeyBlock);
        if (portal == null) {
            return;
        }
        if (portal.getLinkedPortal() == null) {
            return;
        }
        if (!this.plugin.getUsePerms() || player.hasPermission("colorportals.use")) {
            portal.teleport();
        }
        else {
            player.sendMessage(ChatColor.DARK_RED + "You are not authorized to use portals");
        }
    }
    
    @EventHandler
    public void onEntityActivatePressurePlate(final EntityInteractEvent event) {
        if (!this.plugin.getWalkOnActivation()) {
            return;
        }
        final Block portalKeyBlock = event.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP);
        final Portal portal = this.plugin.getPortalHandler().getPortalByKeyBlock(portalKeyBlock);
        if (portal == null) {
            return;
        }
        if (portal.getLinkedPortal() == null) {
            return;
        }
        portal.teleport();
    }
    
    @EventHandler
    public void onExplosion(final EntityExplodeEvent event) {
        final ArrayList<Block> blocksToDestroy = new ArrayList<Block>(50);
        final Iterator<Block> blockIterator = event.blockList().iterator();
        while (blockIterator.hasNext()) {
            final Block block = blockIterator.next();
            Portal portal = null;
            if (BukkitUtils.isSign(block)) {
                portal = this.plugin.getPortalHandler().getPortal(block.getLocation());
            }
            else if (block.getType().name().contains("WOOL") || block.getType().name().contains("BUTTON") || block.getType().name().contains("PRESSURE_PLATE")) {
                portal = this.plugin.getPortalHandler().getPortalByFrameLocation(block.getLocation());
            }
            if (portal != null) {
                if (this.plugin.getPortalProtection()) {
                    blockIterator.remove();
                }
                else {
                    portal.remove();
                }
            }
        }
    }
    
    @EventHandler
    public void onArrowHit(final EntityInteractEvent event) {
        if (!(event.getEntity() instanceof Arrow)) {
            return;
        }
        if (event.getBlock().getType().name().startsWith("STONE")) {
            return;
        }
        final Arrow shot = (Arrow)event.getEntity();
        if (!(shot.getShooter() instanceof Player)) {
            return;
        }
        final Player player = (Player)shot.getShooter();
        final Block portalKeyBlock = event.getBlock().getRelative(BlockFace.UP);
        final Portal portal = this.plugin.getPortalHandler().getPortalByKeyBlock(portalKeyBlock);
        if (portal == null) {
            return;
        }
        if (portal.getLinkedPortal() == null) {
            return;
        }
        if (!this.plugin.getUsePerms() || player.hasPermission("colorportals.use")) {
            portal.teleport();
        }
        else {
            player.sendMessage(ChatColor.DARK_RED + "You are not authorized to use portals (even with arrows)");
        }
    }
    
    public void addNoTeleportEntity(final Entity entity) {
        this.noTeleportEntities.put(entity.getUniqueId(), true);
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, (Runnable)new Runnable() {
            @Override
            public void run() {
                EntityListener.this.noTeleportEntities.remove(entity.getUniqueId());
            }
        }, 5L);
    }
    
    public boolean entityCanBeTeleported(final Entity entity) {
        return this.noTeleportEntities.get(entity.getUniqueId()) == null;
    }
}
