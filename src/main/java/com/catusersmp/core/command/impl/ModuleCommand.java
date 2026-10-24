package com.catusersmp.core.command.impl;

import com.catusersmp.core.CatuserSMPCorePlugin;
import com.catusersmp.core.command.SubCommand;
import com.catusersmp.core.config.Messages;
import com.catusersmp.core.module.Module;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

/**
 * {@code /catuser module ...} - inspect and toggle feature modules at runtime.
 *
 * <pre>
 * /catuser module                  list every module and its state
 * /catuser module &lt;id&gt;             toggle
 * /catuser module &lt;id&gt; on|off      force a state
 * </pre>
 *
 * <p>The new state is written back to {@code config.yml}, so it survives restarts.</p>
 */
public final class ModuleCommand implements SubCommand {

    private final CatuserSMPCorePlugin plugin;

    public ModuleCommand(CatuserSMPCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "module";
    }

    @Override
    public String permission() {
        return "catusersmp.command.module";
    }

    @Override
    public String description() {
        return "查看或开关模块";
    }

    @Override
    public String usage() {
        return "/catuser module [<id> [on|off|toggle]]";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        final Messages messages = this.plugin.messages();
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            messages.send(sender, "module.header");
            for (Module module : this.plugin.modules().all()) {
                sender.sendMessage(messages.component(
                        module.isEnabled() ? "module.entry-on" : "module.entry-off",
                        Placeholder.unparsed("id", module.id())));
            }
            return true;
        }

        final String id = args[0].toLowerCase(Locale.ROOT);
        final Optional<Module> module = this.plugin.modules().byId(id);
        if (module.isEmpty()) {
            messages.send(sender, "module.unknown", Placeholder.unparsed("id", id));
            return true;
        }

        final boolean current = module.get().isEnabled();
        final boolean target;
        if (args.length < 2 || args[1].equalsIgnoreCase("toggle")) {
            target = !current;
        } else if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true")
                || args[1].equalsIgnoreCase("enable")) {
            target = true;
        } else if (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false")
                || args[1].equalsIgnoreCase("disable")) {
            target = false;
        } else {
            messages.send(sender, "module.invalid-value", Placeholder.unparsed("value", args[1]));
            return true;
        }

        this.plugin.modules().setEnabled(id, target);
        messages.send(sender, target ? "module.enabled" : "module.disabled",
                Placeholder.unparsed("id", id));
        return true;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            final List<String> ids = new ArrayList<>(this.plugin.modules().all().stream()
                    .map(Module::id)
                    .toList());
            ids.add("list");
            return ids;
        }
        if (args.length == 2) {
            return List.of("on", "off", "toggle");
        }
        return List.of();
    }
}
