package tij.bot.trello2discord.config;

import tij.bot.trello2discord.config.events.EventTemplate;

import java.util.List;

public record Config(
        String discordChannelId,
        String discordBotToken,
        int port,
        List<ConfigUserMapping> discordTrelloUserMapping,
        List<EventTemplate> events
) {}
