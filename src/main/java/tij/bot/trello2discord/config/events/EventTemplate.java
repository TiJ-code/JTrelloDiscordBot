package tij.bot.trello2discord.config.events;

import java.util.Map;

public record EventTemplate(String name, String title, String body, Map<Integer, FieldTemplate> fields) {
}
