// 
// Decompiled by Procyon v0.5.36
// 

package com.snowgears.colorportals;

import java.util.Set;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;

import com.snowgears.colorportals.utils.BukkitUtils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;
import org.bukkit.DyeColor;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Block;
import java.util.Collection;
import java.util.ArrayList;
import org.bukkit.Location;
import java.util.HashMap;

public class PortalHandler
{
    public ColorPortals plugin;
    private HashMap<Location, Portal> allPortals;
    
    public PortalHandler(final ColorPortals instance) {
        this.plugin = ColorPortals.getPlugin();
        this.allPortals = new HashMap<Location, Portal>();
        this.plugin = instance;
    }
    
    public Portal getPortal(final Location loc) {
        return this.allPortals.get(loc);
    }
    
    public void registerPortal(final Portal portal) {
        this.allPortals.put(portal.getSignLocation(), portal);
        final ArrayList<Portal> portalFamily = this.getPortalFamily(portal);
        if (portalFamily.size() == 1) {
            portal.setLinkedPortal(null);
            return;
        }
        final Portal lastPortal = portalFamily.get(portalFamily.size() - 2);
        lastPortal.setLinkedPortal(portal);
        portal.setLinkedPortal(portalFamily.get(0));
    }
    
    public void deregisterPortal(final Portal portal) {
        if (this.allPortals.containsKey(portal.getSignLocation())) {
            this.allPortals.remove(portal.getSignLocation());
        }
    }
    
    public Collection<Portal> getAllPortals() {
        return this.allPortals.values();
    }
    
    public Portal getPortalByFrameLocation(final Location location) {
        for (int x = -1; x < 2; ++x) {
            for (int y = 0; y < 4; ++y) {
                for (int z = -1; z < 2; ++z) {
                    final Location loc = location.clone().add((double)x, (double)y, (double)z);
                    if (this.getPortal(loc) != null) {
                        final Portal portal = this.getPortal(loc);
                        if (portal.getOccupiedLocations().contains(location)) {
                            return portal;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public Portal getPortalByKeyBlock(final Block portalKeyBlock) {
        if (BukkitUtils.isSign(portalKeyBlock.getRelative(BlockFace.NORTH))) {
            return this.plugin.getPortalHandler().getPortal(portalKeyBlock.getRelative(BlockFace.NORTH).getLocation());
        }
        if (BukkitUtils.isSign(portalKeyBlock.getRelative(BlockFace.EAST))) {
            return this.plugin.getPortalHandler().getPortal(portalKeyBlock.getRelative(BlockFace.EAST).getLocation());
        }
        if (BukkitUtils.isSign(portalKeyBlock.getRelative(BlockFace.SOUTH))) {
            return this.plugin.getPortalHandler().getPortal(portalKeyBlock.getRelative(BlockFace.SOUTH).getLocation());
        }
        if (BukkitUtils.isSign(portalKeyBlock.getRelative(BlockFace.WEST))) {
            return this.plugin.getPortalHandler().getPortal(portalKeyBlock.getRelative(BlockFace.WEST).getLocation());
        }
        return null;
    }
    
    public int getNumberOfPortals() {
        return this.allPortals.size();
    }
    
    public ArrayList<Portal> getPortalFamily(final Portal portal) {
        final ArrayList<Portal> portalFamily = new ArrayList<Portal>();
        for (final Portal checkedPortal : this.plugin.getPortalHandler().getAllPortals()) {
            if (checkedPortal.getChannel() == portal.getChannel() && checkedPortal.getColor().equals((Object)portal.getColor())) {
                portalFamily.add(checkedPortal);
            }
        }
        Collections.sort(portalFamily);
        return portalFamily;
    }
    
    public ArrayList<Portal> getPortalFamily(final Integer channel, final DyeColor color) {
        final ArrayList<Portal> portalFamily = new ArrayList<Portal>();
        for (final Portal checkedPortal : this.plugin.getPortalHandler().getAllPortals()) {
            if (checkedPortal.getChannel() == channel && checkedPortal.getColor().equals((Object)color)) {
                portalFamily.add(checkedPortal);
            }
        }
        Collections.sort(portalFamily);
        return portalFamily;
    }
    
    private ArrayList<Portal> orderedPortalList() {
        final ArrayList<Portal> list = new ArrayList<Portal>(this.allPortals.values());
        Collections.sort(list);
        return list;
    }
    
    public void savePortals() {
        final File fileDirectory = new File(this.plugin.getDataFolder(), "Data");
        if (!fileDirectory.exists()) {
            fileDirectory.mkdir();
        }
        final File portalFile = new File(fileDirectory + "/portals.yml");
        if (!portalFile.exists()) {
            try {
                portalFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(portalFile);
            }
            catch (FileNotFoundException e2) {
                e2.printStackTrace();
            }
            writer.print("");
            writer.close();
        }
        final YamlConfiguration config = YamlConfiguration.loadConfiguration(portalFile);
        final ArrayList<Portal> portalList = this.orderedPortalList();
        for (final Portal portal : portalList) {
            config.set("portals." + portal.getColor().toString() + "." + portal.getChannel() + "-" + portal.getNode() + ".name", (Object)portal.getName());
            config.set("portals." + portal.getColor().toString() + "." + portal.getChannel() + "-" + portal.getNode() + ".location", (Object)this.locationToString(portal.getSignLocation()));
            config.set("portals." + portal.getColor().toString() + "." + portal.getChannel() + "-" + portal.getNode() + ".creator", (Object)portal.getCreator().toString());
        }
        try {
            config.save(portalFile);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public void loadPortals() {
        final File fileDirectory = new File(this.plugin.getDataFolder(), "Data");
        if (!fileDirectory.exists()) {
            return;
        }
        final File portalFile = new File(fileDirectory + "/portals.yml");
        if (!portalFile.exists()) {
            return;
        }
        final YamlConfiguration config = YamlConfiguration.loadConfiguration(portalFile);
        this.loadPortalsFromConfig(config);
    }
    
    private void loadPortalsFromConfig(final YamlConfiguration config) {
        if (config.getConfigurationSection("portals") == null) {
            return;
        }
        final Set<String> allPortalColors = (Set<String>)config.getConfigurationSection("portals").getKeys(false);
        final ArrayList<Portal> portalFamily = new ArrayList<Portal>();
        final Iterator<String> colorIterator = allPortalColors.iterator();
        while (colorIterator.hasNext()) {
            final String portalColor = colorIterator.next();
            final Set<String> allPortalChannels = (Set<String>)config.getConfigurationSection("portals." + portalColor).getKeys(false);
            portalFamily.clear();
            int previousChannel = 0;
            if (allPortalChannels.iterator().hasNext()) {
                final String stringChannel = allPortalChannels.iterator().next();
                final String[] split = stringChannel.split("-");
                previousChannel = Integer.parseInt(split[0]);
            }
            final Iterator<String> channelIterator = allPortalChannels.iterator();
            while (channelIterator.hasNext()) {
                final String portalChannel = channelIterator.next();
                final Location signLocation = this.locationFromString(config.getString("portals." + portalColor + "." + portalChannel + ".location"));
                final Block signBlock = signLocation.getBlock();
                if (BukkitUtils.isSign(signBlock)) {
                    final DyeColor color = DyeColor.valueOf(portalColor);
                    final String[] split2 = portalChannel.split("-");
                    final int channel = Integer.parseInt(split2[0]);
                    final int node = Integer.parseInt(split2[1]);
                    final String name = config.getString("portals." + portalColor + "." + portalChannel + ".name");
                    final String creatorString = config.getString("portals." + portalColor + "." + portalChannel + ".creator");
                    final UUID creator = UUID.fromString(creatorString);
                    final Portal portal = new Portal(creator, name, color, channel, node, signLocation);
                    if (previousChannel != channel) {
                        previousChannel = channel;
                        if (portalFamily.size() == 1) {
                            portalFamily.get(0).setLinkedPortal(null);
                        }
                        else {
                            portalFamily.get(portalFamily.size() - 1).setLinkedPortal(portalFamily.get(0));
                        }
                        for (final Portal p : portalFamily) {
                            this.registerPortal(p);
                        }
                        portalFamily.clear();
                        portalFamily.add(portal);
                    }
                    else {
                        portalFamily.add(portal);
                        if (portalFamily.size() > 1) {
                            portalFamily.get(portalFamily.size() - 2).setLinkedPortal(portalFamily.get(portalFamily.size() - 1));
                        }
                        if (channelIterator.hasNext() && colorIterator.hasNext()) {
                            continue;
                        }
                        for (final Portal p : portalFamily) {
                            this.registerPortal(p);
                        }
                    }
                }
            }
        }
    }
    
    private String locationToString(final Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }
    
    private Location locationFromString(final String loc) {
        final String[] parts = loc.split(",");
        return new Location(this.plugin.getServer().getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }
}
