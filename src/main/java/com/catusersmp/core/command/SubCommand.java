package com.catusersmp.core.command;

import java.util.List;
import org.bukkit.command.CommandSender;

/**
 * One branch of the {@code /catuser} command tree.
 *
 * <p>Implementations are registered in {@link CommandRegistry} and receive the arguments that follow
 * the sub-command name.</p>
 */
public interface SubCommand {

    /** Primary name, for example {@code "reload"}. */
    String name();

    default List<String> aliases() {
        return List.of();
    }

    /** Permission node required to run this sub-command, or {@code null} for everyone. */
    default String permission() {
        return null;
    }

    default String description() {
        return "";
    }

    default String usage() {
        return "/catuser " + name();
    }

    /**
     * @param sender the command sender
     * @param args arguments after the sub-command name
     * @return {@code true} when the command was handled, i.e. no usage message is sent by the caller
     */
    boolean execute(CommandSender sender, String[] args);

    default List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}
