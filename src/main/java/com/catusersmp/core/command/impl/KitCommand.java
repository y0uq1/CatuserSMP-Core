package com.catusersmp.core.command.impl;

import com.catusersmp.core.CatuserSMPCorePlugin;
import com.catusersmp.core.command.SubCommand;
import com.catusersmp.core.config.Messages;
import com.catusersmp.core.kit.Kit;
import com.catusersmp.core.kit.KitManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /catuser kit ...} - save, give and mass-give kits.
 *
 * <pre>
 * /catuser kit save &lt;kit&gt;              save your inventory as a kit (overwrites)
 * /catuser kit load &lt;kit&gt; &lt;player&gt;    give a kit to one player
 * /catuser kit loadall &lt;kit&gt;           give a kit to every online player not in bypass.yml
 * </pre>
 */
public final class KitCommand implements SubCommand {

    private final CatuserSMPCorePlugin plugin;
    private final KitManager kits;

    public KitCommand(CatuserSMPCorePlugin plugin, KitManager kits) {
        this.plugin = plugin;
        this.kits = kits;
    }

    @Override
    public String name() {
        return "kit";
    }

    @Override
    public String permission() {
        return "catusersmp.command.kit";
    }

    @Override
    public String description() {
        return "保存 / 发放 kit";
    }

    @Override
    public String usage() {
        return "/catuser kit <load|save|loadall> <kit> [player]";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        final Messages messages = this.plugin.messages();
        if (args.length == 0) {
            messages.send(sender, "command.usage", Placeholder.unparsed("usage", usage()));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "load" -> load(sender, args);
            case "save" -> save(sender, args);
            case "loadall" -> loadAll(sender, args);
            default -> messages.send(sender, "kit.unknown-sub", Placeholder.unparsed("input", args[0]));
        }
        return true;
    }

    private void load(CommandSender sender, String[] args) {
        final Messages messages = this.plugin.messages();
        if (args.length < 3) {
            messages.send(sender, "command.usage", Placeholder.unparsed("usage", usage()));
            return;
        }

        final Player target = this.plugin.getServer().getPlayerExact(args[2]);
        if (target == null) {
            messages.send(sender, "kit.player-not-found", Placeholder.unparsed("player", args[2]));
            return;
        }

        final Optional<Kit> kit = this.kits.load(args[1]);
        if (kit.isEmpty()) {
            messages.send(sender, "kit.not-found", Placeholder.unparsed("kit", args[1]));
            return;
        }
        if (kit.get().isEmpty()) {
            messages.send(sender, "kit.empty", Placeholder.unparsed("kit", args[1]));
            return;
        }

        this.kits.apply(kit.get(), target);
        messages.send(sender, "kit.given",
                Placeholder.unparsed("kit", kit.get().name()),
                Placeholder.unparsed("player", target.getName()));
        messages.send(target, "kit.received", Placeholder.unparsed("kit", kit.get().name()));
    }

    private void save(CommandSender sender, String[] args) {
        final Messages messages = this.plugin.messages();
        if (!(sender instanceof Player player)) {
            messages.send(sender, "command.player-only");
            return;
        }
        if (args.length < 2) {
            messages.send(sender, "command.usage", Placeholder.unparsed("usage", usage()));
            return;
        }

        final String name = args[1];
        if (!this.kits.isValidName(name)) {
            messages.send(sender, "kit.invalid-name");
            return;
        }

        final boolean overwrite = this.kits.exists(name);
        final int count;
        try {
            count = this.kits.save(name, player);
        } catch (IllegalStateException exception) {
            messages.send(sender, "kit.save-empty");
            return;
        } catch (IOException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "无法保存 kit '" + name + "'", exception);
            messages.send(sender, "kit.save-failed");
            return;
        }

        messages.send(sender, overwrite ? "kit.overwritten" : "kit.saved",
                Placeholder.unparsed("kit", name),
                Placeholder.unparsed("count", Integer.toString(count)));
    }

    private void loadAll(CommandSender sender, String[] args) {
        final Messages messages = this.plugin.messages();
        if (args.length < 2) {
            messages.send(sender, "command.usage", Placeholder.unparsed("usage", usage()));
            return;
        }

        final Optional<Kit> kit = this.kits.load(args[1]);
        if (kit.isEmpty()) {
            messages.send(sender, "kit.not-found", Placeholder.unparsed("kit", args[1]));
            return;
        }
        if (kit.get().isEmpty()) {
            messages.send(sender, "kit.empty", Placeholder.unparsed("kit", args[1]));
            return;
        }

        int given = 0;
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (this.plugin.bypass().contains(player.getUniqueId())) {
                continue;
            }
            this.kits.apply(kit.get(), player);
            messages.send(player, "kit.received", Placeholder.unparsed("kit", kit.get().name()));
            given++;
        }

        messages.send(sender, "kit.load-all",
                Placeholder.unparsed("kit", kit.get().name()),
                Placeholder.unparsed("count", Integer.toString(given)));
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("load", "save", "loadall");
        }
        if (args.length == 2) {
            return new ArrayList<>(this.kits.names());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("load")) {
            return this.plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
