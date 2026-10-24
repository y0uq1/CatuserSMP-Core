package com.catusersmp.core;

import com.catusersmp.core.command.CommandRegistry;
import com.catusersmp.core.command.impl.InfoCommand;
import com.catusersmp.core.command.impl.ReloadCommand;
import com.catusersmp.core.config.ConfigService;
import com.catusersmp.core.config.Messages;
import com.catusersmp.core.module.ModuleManager;
import com.catusersmp.core.module.impl.ChatInterceptorModule;
import com.catusersmp.core.module.impl.HardcoreDeathModule;
import com.catusersmp.core.module.impl.SilentJoinModule;
import com.catusersmp.core.module.impl.SilentQuitModule;
import com.catusersmp.core.util.Tasks;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point of CatuserSMP-Core.
 *
 * <p>Boot order: config -> messages -> utility services -> feature modules -> commands. Any feature that
 * should be toggleable lives in a {@link com.catusersmp.core.module.Module}; this class stays a thin
 * composition root.</p>
 */
public final class CatuserSMPCorePlugin extends JavaPlugin {

    private static CatuserSMPCorePlugin instance;

    private ConfigService configService;
    private Messages messages;
    private Tasks tasks;
    private ModuleManager moduleManager;
    private CommandRegistry commandRegistry;

    /**
     * @return the running plugin instance, or {@code null} while the plugin is disabled
     */
    public static CatuserSMPCorePlugin instance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        final long startedAt = System.nanoTime();

        saveDefaultConfig();

        this.configService = new ConfigService(this);
        this.configService.load();

        this.messages = new Messages(this);
        this.messages.load();

        this.tasks = new Tasks(this);

        this.moduleManager = new ModuleManager(this);
        this.moduleManager.register(new SilentJoinModule(this));
        this.moduleManager.register(new SilentQuitModule(this));
        this.moduleManager.register(new HardcoreDeathModule(this));
        this.moduleManager.register(new ChatInterceptorModule(this));

        this.commandRegistry = new CommandRegistry(this);
        this.commandRegistry.register(new InfoCommand(this));
        this.commandRegistry.register(new ReloadCommand(this));

        bindCommand("catuser");

        this.moduleManager.enableAll();

        getLogger().info(String.format(
                "CatuserSMP-Core v%s enabled in %d ms (%d/%d modules active).",
                getPluginMeta().getVersion(),
                (System.nanoTime() - startedAt) / 1_000_000L,
                this.moduleManager.enabledCount(),
                this.moduleManager.size()));
    }

    @Override
    public void onDisable() {
        if (this.moduleManager != null) {
            this.moduleManager.disableAll();
        }
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("CatuserSMP-Core disabled.");
    }

    /**
     * Reloads config.yml, messages.yml and re-evaluates which modules are enabled.
     * Commands and listeners are re-registered by the module system.
     */
    public void reload() {
        this.configService.load();
        this.messages.load();
        this.moduleManager.reloadAll();
    }

    private void bindCommand(String name) {
        final PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe("Command '" + name + "' is not declared in plugin.yml - commands are unavailable.");
            return;
        }
        command.setExecutor(this.commandRegistry);
        command.setTabCompleter(this.commandRegistry);
    }

    public ConfigService config() {
        return this.configService;
    }

    public Messages messages() {
        return this.messages;
    }

    public Tasks tasks() {
        return this.tasks;
    }

    public ModuleManager modules() {
        return this.moduleManager;
    }

    public CommandRegistry commands() {
        return this.commandRegistry;
    }
}
