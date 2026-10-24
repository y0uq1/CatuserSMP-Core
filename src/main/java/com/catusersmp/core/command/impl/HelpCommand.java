package com.catusersmp.core.command.impl;

import com.catusersmp.core.CatuserSMPCorePlugin;
import com.catusersmp.core.command.CommandRegistry;
import com.catusersmp.core.command.SubCommand;
import com.catusersmp.core.config.Messages;
import java.util.List;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

/** {@code /catuser help} - lists the sub-commands the sender may execute. */
public final class HelpCommand implements SubCommand {

    private final CatuserSMPCorePlugin plugin;
    private final CommandRegistry registry;

    public HelpCommand(CatuserSMPCorePlugin plugin, CommandRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public List<String> aliases() {
        return List.of("?");
    }

    @Override
    public String description() {
        return "显示可用命令列表";
    }

    @Override
    public String usage() {
        return "/catuser help";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        final Messages messages = this.plugin.messages();
        messages.send(sender, "help.header");

        for (SubCommand command : this.registry.visible(sender)) {
            sender.sendMessage(messages.component("help.entry",
                    Placeholder.unparsed("usage", command.usage()),
                    Placeholder.unparsed("description", command.description())));
        }

        messages.send(sender, "help.footer");
        return true;
    }
}
