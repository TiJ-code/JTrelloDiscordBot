package tij.bot.trello2discord.config.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IgnoreRegistry {
    private final Map<String, List<String>> ignores = new HashMap<>();

    public IgnoreRegistry(Map<String, List<String>> map) {
        ignores.putAll(map);
    }

    public List<String> getIgnoredElements(String key) {
        return ignores.get(key);
    }
}
