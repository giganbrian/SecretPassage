package me.reop.secretPassage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class SPPlayerListener implements Listener {
    public static SecretPassage plugin;

    public SPPlayerListener(SecretPassage instance) {
        plugin = instance;
        plugin.getServer().getPluginManager().registerEvents(this, (Plugin)plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        boolean shouldCancel = false;
        if (!event.isCancelled() &&
                event.getAction() == Action.RIGHT_CLICK_BLOCK)
            if (!plugin.switchDestroyer.contains(event.getPlayer().getName())) {
                shouldCancel = plugin.checkSwitch(event.getClickedBlock(), event.getPlayer());
                if (shouldCancel)
                    event.setCancelled(true);
                if (shouldCancel && plugin.conf.toggleNotify)
                    event.getPlayer().sendMessage(plugin.prefix + "Passage toggled.");
            } else {
                plugin.destroySwitch(event.getPlayer(), event.getClickedBlock());
                event.setCancelled(true);
            }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.builders.containsKey(event.getPlayer().getName()))
            plugin.builders.remove(event.getPlayer().getName());
        if (plugin.switchDestroyer.contains(event.getPlayer().getName()))
            plugin.switchDestroyer.remove(event.getPlayer().getName());
    }
}
