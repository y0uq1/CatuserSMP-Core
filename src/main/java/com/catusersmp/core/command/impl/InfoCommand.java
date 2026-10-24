package com.catusersmp.core.command.impl;

import com.catusersmp.core.CatuserSMPCorePlugin;
import com.catusersmp.core.command.SubCommand;
import com.catusersmp.core.config.Messages;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

/** {@code /catuser info} - prints plugin, server and module status. */
public final class InfoCommand implements SubCommand {

    private final CatuserSMPCorePlugin plugin;

    public InfoCommand(CatuserSMPCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "info";
    }

    @Override
    public String permission() {
        return "catusersmp.command.info";
    }

    @Override
    public String description() {
        return "查看插件与服务器信息";
    }

    @Override
    public String usage() {
        return "/catuser info";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        final Messages messages = this.plugin.messages();
        messages.send(sender, "info.header");
        sender.sendMessage(messages.component("info.version",
                Placeholder.unparsed("version", this.plugin.getPluginMeta().getVersion())));
        sender.sendMessage(messages.component("info.server",
                Placeholder.unparsed("server", this.plugin.getServer().getVersion())));
        sender.sendMessage(messages.component("info.java",
                Placeholder.unparsed("java", System.getProperty("java.version"))));
        sender.sendMessage(messages.component("info.modules",
                Placeholder.unparsed("enabled", Long.toString(this.plugin.modules().enabledCount())),
                Placeholder.unparsed("total", Integer.toString(this.plugin.modules().size()))));
        messages.send(sender, "info.footer");
        return true;
    }
}
