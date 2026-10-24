package com.catusersmp.core.config;

import com.catusersmp.core.CatuserSMPCorePlugin;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * UUID based bypass list stored in {@code bypass.yml}.
 *
 * <p>Players in this list are exempt from "clear inventory on quit" and are skipped when a kit is
 * handed out with {@code /catuser kit loadall}. The file is plain YAML so it can be edited by hand;
 * run {@code /catuser reload} (or restart) afterwards.</p>
 */
public final class BypassList {

    private static final String KEY = "bypass";

    private static final String TEMPLATE = """
            # 名单内的玩家：
            #   - 退出服务器时不会被清空背包
            #   - 不会收到 /catuser kit loadall 发放的礼包
            #
            # 填写玩家 UUID（不是玩家名），例如：
            # bypass:
            #   - '069a79f4-44e9-4726-a5be-fca90e38aaf5'
            bypass: []
            """;

    private final CatuserSMPCorePlugin plugin;
    private final File file;
    private final Set<UUID> bypassed = ConcurrentHashMap.newKeySet();

    public BypassList(CatuserSMPCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "bypass.yml");
    }

    public void load() {
        this.bypassed.clear();
        if (!this.file.isFile()) {
            writeTemplate();
            return;
        }

        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.file);
        for (String raw : yaml.getStringList(KEY)) {
            try {
                this.bypassed.add(UUID.fromString(raw.trim()));
            } catch (IllegalArgumentException exception) {
                this.plugin.getLogger().warning("bypass.yml: 无效的 UUID -> " + raw);
            }
        }
        this.plugin.getLogger().info("Loaded " + this.bypassed.size() + " bypass UUID(s).");
    }

    public boolean contains(UUID uuid) {
        return this.bypassed.contains(uuid);
    }

    public Set<UUID> all() {
        return Set.copyOf(this.bypassed);
    }

    /** Adds a player to the list and rewrites the file. @return {@code true} when it was not present */
    public boolean add(UUID uuid) {
        if (this.bypassed.add(uuid)) {
            save();
            return true;
        }
        return false;
    }

    /** Removes a player from the list and rewrites the file. @return {@code true} when it was present */
    public boolean remove(UUID uuid) {
        if (this.bypassed.remove(uuid)) {
            save();
            return true;
        }
        return false;
    }

    private void save() {
        try {
            // Reuse the existing file so the explanatory header comments survive a rewrite.
            if (!this.file.isFile()) {
                writeTemplate();
            }
            final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.file);
            yaml.set(KEY, this.bypassed.stream().map(UUID::toString).sorted().toList());
            yaml.save(this.file);
        } catch (IOException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "无法写入 bypass.yml", exception);
        }
    }

    private void writeTemplate() {
        try {
            Files.createDirectories(this.file.getParentFile().toPath());
            Files.writeString(this.file.toPath(), TEMPLATE, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "无法创建 bypass.yml", exception);
        }
    }
}
