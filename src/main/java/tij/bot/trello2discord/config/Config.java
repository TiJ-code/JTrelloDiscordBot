package tij.bot.trello2discord.config;

import tij.bot.trello2discord.config.events.EventTemplate;

import java.util.List;
import java.util.Map;

public record Config(
        String discordChannelId,
        String discordBotToken,
        int port,
        List<ConfigUserMapping> discordTrelloUserMapping,
        List<EventTemplate> events,
        Map<String, List<String>> ignoredElements
) {}
