package tij.bot.trello2discord.trello.utils;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TemplateRenderer {
    private static final Pattern PATTERN = Pattern.compile("\\{(#.*?)}");

    private TemplateRenderer() {}

    public static String render(JSONObject root, String template) {
        template = template.replace("{!N}", "\n");

        Matcher m = PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();

        while (m.find()) {
            String expr = m.group(1);
            String value = JsonPathResolver.resolve(root, expr);
            m.appendReplacement(sb, Matcher.quoteReplacement(value));
        }

        m.appendTail(sb);
        return sb.toString();
    }
}
