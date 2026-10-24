package com.catusersmp.core.config;

import com.catusersmp.core.CatuserSMPCorePlugin;
import com.catusersmp.core.util.Text;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * MiniMessage based localization layer backed by {@code messages.yml}.
 *
 * <p>Every message is rendered with a {@code <prefix>} tag available for free, and missing keys fall
 * back to the bundled defaults shipped inside the JAR instead of failing silently.</p>
 */
public final class Messages {

    private static final String DEFAULT_PREFIX_KEY = "prefix";

    private final CatuserSMPCorePlugin plugin;
    private FileConfiguration config;
    private String prefix = "";

    public Messages(CatuserSMPCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        final File file = new File(this.plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            this.plugin.saveResource("messages.yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        try (InputStream bundled = this.plugin.getResource("messages.yml")) {
            if (bundled != null) {
                this.config.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(bundled, StandardCharsets.UTF_8)));
            }
        } catch (IOException exception) {
            this.plugin.getLogger().log(Level.WARNING, "Could not read bundled messages.yml defaults", exception);
        }
        this.prefix = this.config.getString(DEFAULT_PREFIX_KEY, "");
    }

    public String prefix() {
        return this.prefix;
    }

    /** Raw MiniMessage source of a key, without parsing. */
    public String raw(String key) {
        final String value = this.config.getString(key);
        if (value == null) {
            this.plugin.getLogger().warning("Missing message key in messages.yml: " + key);
            return "<red>Missing message: " + key;
        }
        return value;
    }

    /** Parses a message key, making {@code <prefix>} and the given resolvers available. */
    public Component component(String key, TagResolver... resolvers) {
        final TagResolver[] all = new TagResolver[resolvers.length + 1];
        all[0] = Placeholder.parsed(DEFAULT_PREFIX_KEY, this.prefix);
        System.arraycopy(resolvers, 0, all, 1, resolvers.length);
        return Text.parse(raw(key), all);
    }

    public void send(Audience audience, String key, TagResolver... resolvers) {
        audience.sendMessage(component(key, resolvers));
    }

    public void send(CommandSender sender, String key, TagResolver... resolvers) {
        sender.sendMessage(component(key, resolvers));
    }
}
