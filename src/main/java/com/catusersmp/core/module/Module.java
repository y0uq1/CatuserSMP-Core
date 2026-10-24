package com.catusersmp.core.module;

import com.catusersmp.core.CatuserSMPCorePlugin;
import java.util.logging.Level;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

/**
 * A self-contained feature that can be toggled from {@code config.yml}.
 *
 * <p>A module that also implements {@link Listener} is registered - and unregistered - automatically,
 * so feature code never leaks listeners across reloads.</p>
 */
public abstract class Module {

    protected final CatuserSMPCorePlugin plugin;

    private boolean enabled;

    protected Module(CatuserSMPCorePlugin plugin) {
        this.plugin = plugin;
    }

    /** Stable identifier, also used as the {@code modules.<id>} config section. */
    public abstract String id();

    public String displayName() {
        return id();
    }

    /** Used when {@code modules.<id>.enabled} is absent from config.yml. */
    public boolean defaultEnabled() {
        return true;
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public final boolean isEnabled() {
        return this.enabled;
    }

    final void enable() {
        if (this.enabled) {
            return;
        }
        try {
            if (this instanceof Listener listener) {
                this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
            }
            onEnable();
            this.enabled = true;
            debug("enabled");
        } catch (Throwable throwable) {
            this.plugin.getLogger().log(Level.SEVERE, "Module '" + id() + "' failed to enable", throwable);
            if (this instanceof Listener listener) {
                HandlerList.unregisterAll(listener);
            }
            this.enabled = false;
        }
    }

    final void disable() {
        if (!this.enabled) {
            return;
        }
        try {
            onDisable();
        } catch (Throwable throwable) {
            this.plugin.getLogger().log(Level.SEVERE, "Module '" + id() + "' failed to disable", throwable);
        } finally {
            if (this instanceof Listener listener) {
                HandlerList.unregisterAll(listener);
            }
            this.enabled = false;
            debug("disabled");
        }
    }

    protected void debug(String message) {
        if (this.plugin.config().debug()) {
            this.plugin.getLogger().info("[debug] [" + id() + "] " + message);
        }
    }
}
