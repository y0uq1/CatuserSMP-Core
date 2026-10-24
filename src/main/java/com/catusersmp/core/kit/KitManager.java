package com.catusersmp.core.kit;

import com.catusersmp.core.CatuserSMPCorePlugin;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Persists kits as YAML files under {@code plugins/CatuserSMP-Core/kits/}.
 *
 * <p>Items are stored as Base64 encoded {@link ItemStack#serializeAsBytes()} payloads, which keeps
 * the full item data - custom names, lore, enchantments, attributes and arbitrary NBT / data
 * components - instead of the lossy Bukkit map serialization.</p>
 */
public final class KitManager {

    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,32}");
    private static final String ITEMS = "items";

    private final CatuserSMPCorePlugin plugin;
    private final File directory;

    public KitManager(CatuserSMPCorePlugin plugin) {
        this.plugin = plugin;
        this.directory = new File(plugin.getDataFolder(), "kits");
    }

    public void setup() {
        if (!this.directory.isDirectory() && !this.directory.mkdirs()) {
            this.plugin.getLogger().warning("无法创建 kits 目录: " + this.directory.getAbsolutePath());
        }
    }

    public File directory() {
        return this.directory;
    }

    public boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    public File fileOf(String name) {
        return new File(this.directory, name + ".yml");
    }

    public boolean exists(String name) {
        return isValidName(name) && fileOf(name).isFile();
    }

    /** @return kit names sorted alphabetically */
    public Set<String> names() {
        final File[] files = this.directory.listFiles((dir, fileName) -> fileName.endsWith(".yml"));
        if (files == null) {
            return Set.of();
        }
        final Set<String> names = new TreeSet<>();
        for (File file : files) {
            names.add(file.getName().substring(0, file.getName().length() - ".yml".length()));
        }
        return names;
    }

    /**
     * Saves (or overwrites) the player's whole inventory, including armor and off-hand.
     *
     * @throws IllegalStateException when the inventory is empty
     */
    public int save(String name, Player player) throws IOException {
        final YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("kit", name);
        yaml.set("saved-at", Instant.now().toString());

        final ItemStack[] contents = player.getInventory().getContents();
        int saved = 0;
        for (int slot = 0; slot < contents.length; slot++) {
            final ItemStack item = contents[slot];
            if (item == null || item.getType().isAir()) {
                continue;
            }
            yaml.set(ITEMS + "." + slot, Base64.getEncoder().encodeToString(item.serializeAsBytes()));
            saved++;
        }
        if (saved == 0) {
            throw new IllegalStateException("empty inventory");
        }

        yaml.save(fileOf(name));
        return saved;
    }

    public Optional<Kit> load(String name) {
        if (!exists(name)) {
            return Optional.empty();
        }

        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(fileOf(name));
        final Map<Integer, ItemStack> items = new TreeMap<>();
        final ConfigurationSection section = yaml.getConfigurationSection(ITEMS);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                final String encoded = section.getString(key);
                if (encoded == null) {
                    continue;
                }
                try {
                    items.put(Integer.parseInt(key), ItemStack.deserializeBytes(Base64.getDecoder().decode(encoded)));
                } catch (RuntimeException exception) {
                    this.plugin.getLogger().warning(
                            "读取 kit '" + name + "' 的槽位 " + key + " 失败: " + exception.getMessage());
                }
            }
        }
        return Optional.of(new Kit(name, items));
    }

    /**
     * Hands the kit to the player. Items go back into their original slot when it is free, otherwise
     * they are merged into the first free slot and finally dropped at the player's feet - existing
     * items are never destroyed.
     */
    public void apply(Kit kit, Player player) {
        final PlayerInventory inventory = player.getInventory();
        final List<ItemStack> overflow = new ArrayList<>();

        for (Map.Entry<Integer, ItemStack> entry : kit.items().entrySet()) {
            final ItemStack item = entry.getValue().clone();
            final int slot = entry.getKey();
            if (slot >= 0 && slot < inventory.getSize() && isEmpty(inventory.getItem(slot))) {
                inventory.setItem(slot, item);
            } else {
                overflow.add(item);
            }
        }

        if (!overflow.isEmpty()) {
            final Map<Integer, ItemStack> leftovers = inventory.addItem(overflow.toArray(ItemStack[]::new));
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
    }

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().isAir();
    }
}
