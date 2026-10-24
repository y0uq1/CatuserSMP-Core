package com.catusersmp.core.module.impl;

import com.catusersmp.core.CatuserSMPCorePlugin;
import com.catusersmp.core.module.Module;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Hides the vanilla "left the game" broadcast and prints a red {@code [-] <player>} line to the
 * console instead.
 */
public final class SilentQuitModule extends Module implements Listener {

    public SilentQuitModule(CatuserSMPCorePlugin plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "silent-quit";
    }

    @Override
    public String displayName() {
        return "SilentQuit";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // No broadcast to players at all.
        event.quitMessage(null);
        this.plugin.messages().send(this.plugin.getServer().getConsoleSender(), "console.quit",
                Placeholder.unparsed("player", event.getPlayer().getName()));
    }
}
