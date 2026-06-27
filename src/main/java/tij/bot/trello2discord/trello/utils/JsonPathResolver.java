package tij.bot.trello2discord.trello.utils;

import org.json.JSONObject;

public final class JsonPathResolver {
    private JsonPathResolver() {}

    public static String resolve(JSONObject root, String path) {
        if (path == null || !path.startsWith("#")) return path;

        String[] parts = path.substring(1).split("\\.");

        Object current = root;

        for (String part : parts) {

            if (current instanceof JSONObject obj) {
                current = obj.opt(part);
            } else {
                return "";
            }

            if (current == null || current == JSONObject.NULL) {
                return "";
            }
        }

        return String.valueOf(current);
    }
}
