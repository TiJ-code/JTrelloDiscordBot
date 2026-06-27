package tij.bot.trello2discord.config.events;

import tij.bot.trello2discord.common.MessageType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EventRegistry {
    private final Map<MessageType, EventTemplate> templates = new EnumMap<>(MessageType.class);

    public EventRegistry(List<EventTemplate> list) {
        for (EventTemplate t : list) {
            templates.put(
                    MessageType.valueOf(t.name()),
                    t
            );
        }
    }

    public EventTemplate get(MessageType type) {
        return templates.get(type);
    }
}
