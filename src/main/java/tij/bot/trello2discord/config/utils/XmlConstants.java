package tij.bot.trello2discord.config.utils;

public final class XmlConstants {
    public static final String TAG_CONFIG = "config";
    public static final String TAG_ENTRY = "entry";
    public static final String TAG_USER_MAPPINGS = "userMapping";
    public static final String TAG_USER = "user";

    public static final String ATTRIBUTE_CONFIG_VERSION = "version";
    public static final String ATTRIBUTE_ENTRY_NAME = "name";
    public static final String ATTRIBUTE_USER_DISCORD = "discord.user.id";
    public static final String ATTRIBUTE_USER_TRELLO = "trello.user.id";


    public static final String ENTRY_DISCORD_CHANNEL_ID = "discord.channel.id";
    public static final String ENTRY_DISCORD_BOT_TOKEN = "discord.bot.token";
    public static final String ENTRY_PORT = "port";


    private XmlConstants() {}
}
