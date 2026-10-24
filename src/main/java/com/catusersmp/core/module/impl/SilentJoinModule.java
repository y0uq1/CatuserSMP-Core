package com.catusersmp.core.module.impl;

import com.catusersmp.core.CatuserSMPCorePlugin;
import com.catusersmp.core.module.Module;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Hides the vanilla "joined the game" broadcast and prints a red {@code [+] <player>} line to the
 * console instead.
 */
public final class SilentJoinModule extends Module implements Listener {

    public SilentJoinModule(CatuserSMPCorePlugin plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "silent-join";
    }

    @Override
    public String displayName() {
        return "SilentJoin";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // No broadcast to players at all.
        event.joinMessage(null);
        this.plugin.messages().send(this.plugin.getServer().getConsoleSender(), "console.join",
                Placeholder.unparsed("player", event.getPlayer().getName()));
    }
}
