package me.reop.secretPassage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.plugin.Plugin;

public class SPRedstoneListener implements Listener {
    public static SecretPassage plugin;

    public SPRedstoneListener(SecretPassage instance) {
        plugin = instance;
        plugin.getServer().getPluginManager().registerEvents(this, (Plugin)plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if (plugin.conf.useRedstone)
            plugin.redstoneToggle(event.getBlock().getWorld());
    }
}
