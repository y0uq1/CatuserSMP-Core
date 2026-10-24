package com.catusersmp.core.config;

import com.catusersmp.core.CatuserSMPCorePlugin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Typed access to {@code config.yml}. Read values through the helpers below so a missing or
 * malformed key never blows up a feature at runtime.
 */
public final class ConfigService {

    private final CatuserSMPCorePlugin plugin;
    private FileConfiguration config;

    public ConfigService(CatuserSMPCorePlugin plugin) {
        this.plugin = plugin;
    }

    /** Re-reads {@code config.yml} from disk, recreating it from the bundled defaults when missing. */
    public void load() {
        this.plugin.saveDefaultConfig();
        this.plugin.reloadConfig();
        this.config = this.plugin.getConfig();
    }

    public FileConfiguration raw() {
        return this.config;
    }

    public boolean debug() {
        return booleanValue("settings.debug", false);
    }

    /** Whether a module should be enabled, falling back to the module's own default. */
    public boolean moduleEnabled(String moduleId, boolean fallback) {
        return booleanValue("modules." + moduleId + ".enabled", fallback);
    }

    public boolean booleanValue(String path, boolean fallback) {
        return this.config.getBoolean(path, fallback);
    }

    public int intValue(String path, int fallback) {
        return this.config.getInt(path, fallback);
    }

    public long longValue(String path, long fallback) {
        return this.config.getLong(path, fallback);
    }

    public double doubleValue(String path, double fallback) {
        return this.config.getDouble(path, fallback);
    }

    public String stringValue(String path, String fallback) {
        return this.config.getString(path, fallback);
    }

    public void save() {
        this.plugin.saveConfig();
    }
}
