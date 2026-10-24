package com.catusersmp.core.command.impl;

import com.catusersmp.core.CatuserSMPCorePlugin;
import com.catusersmp.core.command.SubCommand;
import java.util.List;
import java.util.logging.Level;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

/** {@code /catuser reload} - reloads config.yml, messages.yml and every module. */
public final class ReloadCommand implements SubCommand {

    private final CatuserSMPCorePlugin plugin;

    public ReloadCommand(CatuserSMPCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public List<String> aliases() {
        return List.of("rl");
    }

    @Override
    public String permission() {
        return "catusersmp.command.reload";
    }

    @Override
    public String description() {
        return "重新加载配置与模块";
    }

    @Override
    public String usage() {
        return "/catuser reload";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        final long startedAt = System.nanoTime();
        try {
            this.plugin.reload();
            this.plugin.messages().send(sender, "reload.success", Placeholder.unparsed(
                    "time", Long.toString((System.nanoTime() - startedAt) / 1_000_000L)));
        } catch (RuntimeException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "Reload failed", exception);
            this.plugin.messages().send(sender, "reload.failed");
        }
        return true;
    }
}
