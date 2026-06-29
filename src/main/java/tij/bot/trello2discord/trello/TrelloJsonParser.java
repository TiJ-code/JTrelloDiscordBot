package tij.bot.trello2discord.trello;

import org.json.JSONObject;
import tij.bot.trello2discord.common.Message;
import tij.bot.trello2discord.common.MessageType;
import tij.bot.trello2discord.config.events.EventRegistry;
import tij.bot.trello2discord.config.events.EventTemplate;
import tij.bot.trello2discord.config.events.FieldTemplate;
import tij.bot.trello2discord.config.utils.IgnoreRegistry;
import tij.bot.trello2discord.config.utils.XmlConstants;
import tij.bot.trello2discord.trello.utils.JsonConstants;
import tij.bot.trello2discord.trello.utils.TemplateRenderer;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class TrelloJsonParser {
    private final EventRegistry registry;
    private final IgnoreRegistry ignoreRegistry;

    public TrelloJsonParser(EventRegistry registry, IgnoreRegistry ignoreRegistry) {
        this.registry = registry;
        this.ignoreRegistry = ignoreRegistry;
    }

    public Optional<Message> parse(JSONObject json) {
        System.out.println(json);

        JSONObject action = json.optJSONObject(JsonConstants.OBJECT_ACTION);
        if (action == null) return Optional.empty();

        if (shouldIgnore(action)) {
            return Optional.empty();
        }

        MessageType type = TrelloEventResolver.resolve(action);
        if (type == MessageType.INVALID) return Optional.empty();

        EventTemplate template = registry.get(type);
        if (template == null) return Optional.empty();

        JSONObject data = action.optJSONObject(JsonConstants.OBJECT_DATA);

        String memberName =
                action.optJSONObject(JsonConstants.OBJECT_MEMBER_CREATOR)
                        .optString(JsonConstants.FIELD_FULL_NAME, "Unknown");

        String boardName =
                data != null && data.optJSONObject(JsonConstants.OBJECT_BOARD) != null
                        ? data.getJSONObject(JsonConstants.OBJECT_BOARD)
                        .optString(JsonConstants.FIELD_NAME, "Board")
                        : "Board";

        Message.Builder builder = new Message.Builder(
                type,
                boardName,
                memberName
        );

        builder.title(TemplateRenderer.render(action, template.title()));
        builder.body(TemplateRenderer.render(action, template.body()));

        for (FieldTemplate field : template.fields().values()) {
            String title = TemplateRenderer.render(action, field.title());
            String body = TemplateRenderer.render(action, field.body());

            builder.addField(title, body);
        }

        return Optional.of(builder.build());
    }

    private boolean shouldIgnore(JSONObject action) {
        return checkIgnore(
                XmlConstants.IGNORE_KEY_VALUE_USER_ID,
                getUserId(action)
        ) || checkIgnore(
                XmlConstants.IGNORE_KEY_VALUE_LIST_ID,
                getListId(action)
        ) || checkIgnoredFormat(
                XmlConstants.IGNORE_KEY_VALUE_CARD_NAME_FORMAT,
                getCardName(action)
        );
    }

    private boolean checkIgnore(String ignoreKey, String value) {
        if (value == null) {
            return false;
        }

        List<String> ignored = ignoreRegistry.getIgnoredElements(ignoreKey);

        return ignored != null && ignored.contains(value);
    }

    private boolean checkIgnoredFormat(String ignoreKey, String value) {
        if (value == null) {
            return false;
        }

        List<String> ignored = ignoreRegistry.getIgnoredElements(ignoreKey);

        if (ignored == null) {
            return false;
        }

        return ignored.stream().anyMatch(ignoreFormat -> Pattern.compile(ignoreFormat).matcher(value).find());
    }

    private String getUserId(JSONObject action) {
        JSONObject member = action.optJSONObject(JsonConstants.OBJECT_MEMBER_CREATOR);

        if (member == null) {
            return null;
        }

        return member.optString("id", null);
    }

    private String getListId(JSONObject action) {
        JSONObject data = action.optJSONObject(JsonConstants.OBJECT_DATA);

        if (data == null) {
            return null;
        }

        JSONObject list = data.optJSONObject(JsonConstants.OBJECT_LIST);

        if (list == null) {
            return null;
        }

        return list.optString("id", null);
    }

    private String getCardName(JSONObject action) {
        JSONObject data = action.optJSONObject(JsonConstants.OBJECT_DATA);

        if (data == null) {
            return null;
        }

        JSONObject card = data.optJSONObject(JsonConstants.OBJECT_CARD);

        if (card == null) {
            return null;
        }

        return card.optString(JsonConstants.FIELD_NAME, null);
    }
}