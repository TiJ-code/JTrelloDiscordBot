package tij.bot.trello2discord.config.utils;

import tij.bot.trello2discord.common.MessageType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class XmlConstants {
    public static final int VERSION = 4;

    public static final String TAG_CONFIG = "config";
    public static final String TAG_ENTRY = "entry";
    public static final String TAG_USER_MAPPINGS = "userMapping";
    public static final String TAG_USER = "user";
    public static final String TAG_EVENTS = "events";
    public static final String TAG_EVENT = "event";
    public static final String TAG_FORMAT = "format";
    public static final String TAG_FIELDS = "fields";
    public static final String TAG_FIELD = "field";
    public static final String TAG_IGNORES = "ignores";
    public static final String TAG_IGNORE = "ignore";
    public static final String TAG_ELEMENT = "element";

    public static final String ATTRIBUTE_CONFIG_VERSION = "version";
    public static final String ATTRIBUTE_ENTRY_NAME = "name";
    public static final String ATTRIBUTE_USER_DISCORD = "discord.user.id";
    public static final String ATTRIBUTE_USER_TRELLO = "trello.user.id";
    public static final String ATTRIBUTE_EVENT_NAME = "name";
    public static final String ATTRIBUTE_FIELD_INLINE = "inline";
    public static final String ATTRIBUTE_FORMAT_KEY = "key";
    public static final String ATTRIBUTE_IGNORE_KEY = "key";


    public static final Set<String> ATTRIBUTE_EVENT_NAME_VALUES = Arrays.stream(MessageType.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    public static final String IGNORE_KEY_VALUE_LIST_ID = "trello.list.id";
    public static final String IGNORE_KEY_VALUE_USER_ID = "trello.user.id";
    public static final String IGNORE_KEY_VALUE_CARD_NAME_FORMAT = "trello.card.name.format";

    public static final Set<String> ATTRIBUTE_IGNORE_KEY_VALUES = Set.of(
            IGNORE_KEY_VALUE_LIST_ID,
            IGNORE_KEY_VALUE_USER_ID,
            IGNORE_KEY_VALUE_CARD_NAME_FORMAT
    );

    public static final String ATTR_FORMAT_KEY_VALUE_TITLE = "title";
    public static final String ATTR_FORMAT_KEY_VALUE_BODY = "body";


    public static final String ENTRY_DISCORD_CHANNEL_ID = "discord.channel.id";
    public static final String ENTRY_DISCORD_BOT_TOKEN = "discord.bot.token";
    public static final String ENTRY_PORT = "port";


    private XmlConstants() {}
}
