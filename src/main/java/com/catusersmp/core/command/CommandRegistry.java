package com.catusersmp.core.command;

import com.catusersmp.core.CatuserSMPCorePlugin;
import com.catusersmp.core.command.impl.HelpCommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

/** Dispatches {@code /catuser <sub>} to the matching {@link SubCommand} and handles tab completion. */
public final class CommandRegistry implements CommandExecutor, TabCompleter {

    private final CatuserSMPCorePlugin plugin;
    private final Map<String, SubCommand> lookup = new LinkedHashMap<>();
    private final List<SubCommand> ordered = new ArrayList<>();
    private final SubCommand fallback;

    public CommandRegistry(CatuserSMPCorePlugin plugin) {
        this.plugin = plugin;
        this.fallback = new HelpCommand(plugin, this);
        register(this.fallback);
    }

    public void register(SubCommand command) {
        if (!this.ordered.contains(command)) {
            this.ordered.add(command);
        }
        this.lookup.putIfAbsent(command.name().toLowerCase(Locale.ROOT), command);
        for (String alias : command.aliases()) {
            this.lookup.putIfAbsent(alias.toLowerCase(Locale.ROOT), command);
        }
    }

    /** Sub-commands the sender is allowed to see in {@code /catuser help}. */
    public List<SubCommand> visible(CommandSender sender) {
        return this.ordered.stream()
                .filter(command -> command.permission() == null || sender.hasPermission(command.permission()))
                .toList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return this.fallback.execute(sender, args);
        }

        final SubCommand sub = this.lookup.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            this.plugin.messages().send(sender, "command.unknown", Placeholder.unparsed("input", args[0]));
            return true;
        }
        if (sub.permission() != null && !sender.hasPermission(sub.permission())) {
            this.plugin.messages().send(sender, "command.no-permission");
            return true;
        }

        final String[] rest = Arrays.copyOfRange(args, 1, args.length);
        try {
            return sub.execute(sender, rest);
        } catch (RuntimeException exception) {
            this.plugin.getLogger().log(Level.SEVERE,
                    "Sub-command '" + label + " " + sub.name() + "' failed", exception);
            this.plugin.messages().send(sender, "command.error");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return filter(visibleNames(sender), "");
        }
        if (args.length == 1) {
            return filter(visibleNames(sender), args[0]);
        }

        final SubCommand sub = this.lookup.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null || (sub.permission() != null && !sender.hasPermission(sub.permission()))) {
            return List.of();
        }

        final String[] rest = Arrays.copyOfRange(args, 1, args.length);
        return filter(sub.complete(sender, rest), rest[rest.length - 1]);
    }

    private List<String> visibleNames(CommandSender sender) {
        return visible(sender).stream().map(SubCommand::name).toList();
    }

    private static List<String> filter(List<String> candidates, String prefix) {
        final String lowered = prefix.toLowerCase(Locale.ROOT);
        return candidates.stream()
                .filter(candidate -> candidate.toLowerCase(Locale.ROOT).startsWith(lowered))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
