package com.catusersmp.core.module.impl;

import com.catusersmp.core.CatuserSMPCorePlugin;
import com.catusersmp.core.module.Module;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Hardcore death handling: put the player into spectator mode on death and keep them at the place
 * where they died.
 *
 * <p>Two hooks are needed because vanilla moves the player back to the world spawn on respawn:
 * {@link PlayerDeathEvent} switches the mode and teleports immediately, and
 * {@link PlayerRespawnEvent} pins the respawn location to the death spot so the player does not end
 * up at spawn.</p>
 */
public final class HardcoreDeathModule extends Module implements Listener {

    private final Map<UUID, Location> deathLocations = new ConcurrentHashMap<>();

    public HardcoreDeathModule(CatuserSMPCorePlugin plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "hardcore-death";
    }

    @Override
    public String displayName() {
        return "HardcoreDeath";
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        if (!appliesTo(player)) {
            return;
        }

        final Location deathLocation = player.getLocation().clone();
        this.deathLocations.put(player.getUniqueId(), deathLocation);

        // Defer one tick: changing game mode mid-death is otherwise overwritten by the death handling.
        this.plugin.tasks().run(() -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(deathLocation);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Location deathLocation = this.deathLocations.remove(event.getPlayer().getUniqueId());
        if (deathLocation == null) {
            return;
        }

        // Overrides the vanilla "respawn at world spawn" behaviour.
        event.setRespawnLocation(deathLocation);

        final Player player = event.getPlayer();
        this.plugin.tasks().run(() -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(deathLocation);
        });
    }

    private boolean appliesTo(Player player) {
        return !this.plugin.config().booleanValue("modules.hardcore-death.only-hardcore", true)
                || player.getWorld().isHardcore();
    }

    @Override
    protected void onDisable() {
        this.deathLocations.clear();
    }
}
