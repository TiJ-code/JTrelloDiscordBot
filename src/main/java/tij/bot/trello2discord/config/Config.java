package tij.bot.trello2discord.config;

import java.util.List;

public record Config(
        String discordChannelId,
        String discordBotToken,
        int port,
        List<ConfigUserMapping> discordTrelloUserMapping
) {}
