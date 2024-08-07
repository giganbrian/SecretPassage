package me.reop.secretPassage;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

public class SPBlockListener implements Listener {
    public static SecretPassage plugin;

    public SPBlockListener(SecretPassage instance) {
        plugin = instance;
        plugin.getServer().getPluginManager().registerEvents(this, (Plugin)plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockDamage(BlockDamageEvent event) {
        if (plugin.builders.containsKey(event.getPlayer().getName()) && plugin.conf.activeMaterials
                .contains(event.getBlock().getType()) &&
                !event.isCancelled())
            plugin.constructPassage(event.getPlayer(), event.getBlock());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        boolean shouldCancel = false;
        Block block = event.getBlock();
        if (plugin.builders.containsKey(event.getPlayer().getName()) && plugin.conf.activeMaterials
                .contains(block.getType()) && event
                .getPlayer().getGameMode() == GameMode.CREATIVE &&
                !event.isCancelled()) {
            plugin.constructPassage(event.getPlayer(), block);
            event.setCancelled(true);
        } else {
            if (plugin.builders.containsKey(event.getPlayer().getName()) && !event.isCancelled())
                plugin.destructPassage(event.getPlayer(), block);
            if (plugin.conf.protectBlocks && !event.isCancelled()) {
                shouldCancel = plugin.protectPassages(block);
                if (shouldCancel && plugin.conf.protectNotify)
                    event.getPlayer().sendMessage(plugin.prefix + "You are not allowed to break this block.");
                event.setCancelled(shouldCancel);
            }
            if (plugin.passMan.findSwitch(block) != -1)
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && plugin.passMan.findSwitch(block) != -1) {
                plugin.checkSwitch(block, event.getPlayer());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!event.isCancelled() && plugin.conf.protectBlocks) {
            boolean shouldCancel = plugin.checkBlocks(event.getBlocks());
            event.setCancelled(shouldCancel);
            if (shouldCancel)
                event.getBlock().breakNaturally();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!event.isCancelled() && plugin.conf.protectBlocks) {
            Block myBlock = event.getBlock().getWorld().getBlockAt(event.getRetractLocation());
            boolean shouldCancel = plugin.protectPassages(myBlock);
            event.setCancelled(shouldCancel);
        }
    }
}
