package com.catusersmp.core.kit;

import java.util.Map;
import org.bukkit.inventory.ItemStack;

/**
 * A stored kit: items keyed by the inventory slot they were saved from.
 *
 * @param name kit name (file name without {@code .yml})
 * @param items slot index to item; iteration order is ascending by slot
 */
public record Kit(String name, Map<Integer, ItemStack> items) {

    public int size() {
        return this.items.size();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }
}
