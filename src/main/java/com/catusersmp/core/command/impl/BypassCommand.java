package com.catusersmp.core.command.impl;

import com.catusersmp.core.CatuserSMPCorePlugin;
import com.catusersmp.core.command.SubCommand;
import com.catusersmp.core.config.Messages;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /catuser bypass ...} - manage the UUID list in {@code bypass.yml}.
 *
 * <pre>
 * /catuser bypass add &lt;player|uuid&gt;
 * /catuser bypass remove &lt;player|uuid&gt;
 * /catuser bypass list
 * </pre>
 *
 * <p>Names are resolved against online players first, then against the server's offline player
 * cache; UUIDs can also be passed directly, which works even for players that never joined.</p>
 */
public final class BypassCommand implements SubCommand {

    private final CatuserSMPCorePlugin plugin;

    public BypassCommand(CatuserSMPCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "bypass";
    }

    @Override
    public String permission() {
        return "catusersmp.command.bypass";
    }

    @Override
    public String description() {
        return "管理 bypass.yml 名单";
    }

    @Override
    public String usage() {
        return "/catuser bypass <add|remove|list> [player|uuid]";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        final Messages messages = this.plugin.messages();
        if (args.length == 0) {
            messages.send(sender, "command.usage", Placeholder.unparsed("usage", usage()));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "add" -> add(sender, args);
            case "remove" -> remove(sender, args);
            case "list" -> list(sender);
            default -> messages.send(sender, "bypass.unknown-sub", Placeholder.unparsed("input", args[0]));
        }
        return true;
    }

    private void add(CommandSender sender, String[] args) {
        final Messages messages = this.plugin.messages();
        if (args.length < 2) {
            messages.send(sender, "command.usage", Placeholder.unparsed("usage", usage()));
            return;
        }

        final Optional<Target> target = resolve(args[1]);
        if (target.isEmpty()) {
            messages.send(sender, "bypass.player-not-found", Placeholder.unparsed("player", args[1]));
            return;
        }

        final boolean added = this.plugin.bypass().add(target.get().uuid());
        if (!added) {
            messages.send(sender, "bypass.already",
                    Placeholder.unparsed("player", target.get().label()),
                    Placeholder.unparsed("uuid", target.get().uuid().toString()));
            return;
        }
        messages.send(sender, target.get().fromUuid() ? "bypass.added-uuid" : "bypass.added",
                Placeholder.unparsed("player", target.get().label()),
                Placeholder.unparsed("uuid", target.get().uuid().toString()));
    }

    private void remove(CommandSender sender, String[] args) {
        final Messages messages = this.plugin.messages();
        if (args.length < 2) {
            messages.send(sender, "command.usage", Placeholder.unparsed("usage", usage()));
            return;
        }

        final Optional<Target> target = resolve(args[1]);
        if (target.isEmpty()) {
            messages.send(sender, "bypass.player-not-found", Placeholder.unparsed("player", args[1]));
            return;
        }

        final boolean removed = this.plugin.bypass().remove(target.get().uuid());
        if (!removed) {
            messages.send(sender, "bypass.not-present",
                    Placeholder.unparsed("player", target.get().label()),
                    Placeholder.unparsed("uuid", target.get().uuid().toString()));
            return;
        }
        messages.send(sender, target.get().fromUuid() ? "bypass.removed-uuid" : "bypass.removed",
                Placeholder.unparsed("player", target.get().label()),
                Placeholder.unparsed("uuid", target.get().uuid().toString()));
    }

    private void list(CommandSender sender) {
        final Messages messages = this.plugin.messages();
        final List<UUID> entries = this.plugin.bypass().all().stream().sorted().toList();
        if (entries.isEmpty()) {
            messages.send(sender, "bypass.list-empty");
            return;
        }

        messages.send(sender, "bypass.list-header",
                Placeholder.unparsed("count", Integer.toString(entries.size())));
        for (UUID uuid : entries) {
            sender.sendMessage(messages.component("bypass.list-entry",
                    Placeholder.unparsed("uuid", uuid.toString())));
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("add", "remove", "list");
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("list")) {
            return this.plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }

    /** A resolved UUID, the label to show back to the sender, and whether the input was a raw UUID. */
    private record Target(UUID uuid, String label, boolean fromUuid) {
    }

    private Optional<Target> resolve(String input) {
        try {
            return Optional.of(new Target(UUID.fromString(input), input, true));
        } catch (IllegalArgumentException ignored) {
            // not a UUID, fall through to a name lookup
        }

        final Player online = this.plugin.getServer().getPlayerExact(input);
        if (online != null) {
            return Optional.of(new Target(online.getUniqueId(), online.getName(), false));
        }

        final OfflinePlayer cached = this.plugin.getServer().getOfflinePlayerIfCached(input);
        if (cached != null) {
            return Optional.of(new Target(cached.getUniqueId(), input, false));
        }
        return Optional.empty();
    }
}
