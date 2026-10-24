package com.catusersmp.core.module.impl;

import com.catusersmp.core.CatuserSMPCorePlugin;
import com.catusersmp.core.module.Module;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Wipes a player's inventory when they leave the server.
 *
 * <p>Server operators and UUIDs listed in {@code bypass.yml} are exempt.</p>
 */
public final class ClearInventoryModule extends Module implements Listener {

    public ClearInventoryModule(CatuserSMPCorePlugin plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "clear-inventory";
    }

    @Override
    public String displayName() {
        return "ClearInventory";
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (player.isOp() || this.plugin.bypass().contains(player.getUniqueId())) {
            debug("keeping inventory of " + player.getName() + " (op/bypass)");
            return;
        }

        player.getInventory().clear();
        debug("cleared inventory of " + player.getName());
    }
}
