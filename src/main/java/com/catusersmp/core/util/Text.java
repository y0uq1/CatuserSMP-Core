package com.catusersmp.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/** Adventure / MiniMessage helpers shared by the whole plugin. */
public final class Text {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private Text() {
    }

    /** Parses MiniMessage, falling back to plain text when the input contains invalid tags. */
    public static Component parse(String input, TagResolver... resolvers) {
        try {
            return MINI_MESSAGE.deserialize(input, resolvers);
        } catch (RuntimeException exception) {
            return Component.text(input);
        }
    }

    /** Removes MiniMessage tags, useful for tab completion, titles or logging. */
    public static String stripTags(String input) {
        return MINI_MESSAGE.stripTags(input);
    }

    /** Serializes a component to plain text without formatting. */
    public static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
