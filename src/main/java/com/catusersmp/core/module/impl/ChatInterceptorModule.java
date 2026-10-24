package com.catusersmp.core.module.impl;

import com.catusersmp.core.CatuserSMPCorePlugin;
import com.catusersmp.core.module.Module;
import com.catusersmp.core.util.Text;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Intercepts player chat before it is broadcast.
 *
 * <p>Two modes, controlled by {@code modules.chat-interceptor.block-all}: block every message, or
 * only the ones containing a configured keyword. Blocked messages never reach other players; they
 * are optionally reported to the console and to the sender.</p>
 */
public final class ChatInterceptorModule extends Module implements Listener {

    public ChatInterceptorModule(CatuserSMPCorePlugin plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "chat-interceptor";
    }

    @Override
    public String displayName() {
        return "ChatInterceptor";
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        final String message = Text.plain(event.message());
        if (!shouldBlock(message)) {
            return;
        }

        event.setCancelled(true);
        debug("blocked a message from " + event.getPlayer().getName());

        if (this.plugin.config().booleanValue("modules.chat-interceptor.log-blocked", true)) {
            this.plugin.messages().send(this.plugin.getServer().getConsoleSender(), "console.chat-blocked",
                    Placeholder.unparsed("player", event.getPlayer().getName()),
                    Placeholder.unparsed("message", message));
        }

        if (this.plugin.config().booleanValue("modules.chat-interceptor.notify-sender", true)) {
            this.plugin.messages().send(event.getPlayer(), "chat.blocked");
        }
    }

    private boolean shouldBlock(String message) {
        if (this.plugin.config().booleanValue("modules.chat-interceptor.block-all", true)) {
            return true;
        }

        final List<String> blockedWords = this.plugin.config().raw()
                .getStringList("modules.chat-interceptor.blocked-words");
        if (blockedWords.isEmpty()) {
            return false;
        }

        final String lowered = message.toLowerCase(Locale.ROOT);
        for (String word : blockedWords) {
            if (!word.isBlank() && lowered.contains(word.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
