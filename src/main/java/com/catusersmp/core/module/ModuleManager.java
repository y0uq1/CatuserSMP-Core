package com.catusersmp.core.module;

import com.catusersmp.core.CatuserSMPCorePlugin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Owns every {@link Module}: registration, config-driven enabling, and ordered shutdown. */
public final class ModuleManager {

    private final CatuserSMPCorePlugin plugin;
    private final Map<String, Module> modules = new LinkedHashMap<>();

    public ModuleManager(CatuserSMPCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register(Module module) {
        final Module previous = this.modules.putIfAbsent(module.id(), module);
        if (previous != null) {
            throw new IllegalStateException("Duplicate module id: " + module.id());
        }
    }

    public void enableAll() {
        for (Module module : this.modules.values()) {
            if (this.plugin.config().moduleEnabled(module.id(), module.defaultEnabled())) {
                module.enable();
            } else {
                this.plugin.getLogger().info("Module '" + module.id() + "' is disabled in config.yml");
            }
        }
    }

    public void disableAll() {
        final List<Module> reversed = new ArrayList<>(this.modules.values());
        Collections.reverse(reversed);
        for (Module module : reversed) {
            module.disable();
        }
    }

    public void reloadAll() {
        disableAll();
        enableAll();
    }

    public Optional<Module> byId(String id) {
        return Optional.ofNullable(this.modules.get(id));
    }

    public Collection<Module> all() {
        return Collections.unmodifiableCollection(this.modules.values());
    }

    public long enabledCount() {
        return this.modules.values().stream().filter(Module::isEnabled).count();
    }

    public int size() {
        return this.modules.size();
    }
}
