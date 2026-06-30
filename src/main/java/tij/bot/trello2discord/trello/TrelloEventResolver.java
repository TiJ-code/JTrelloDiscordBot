package tij.bot.trello2discord.trello;

import org.json.JSONObject;
import tij.bot.trello2discord.common.MessageType;
import tij.bot.trello2discord.trello.utils.JsonConstants;

public final class TrelloEventResolver {
    private TrelloEventResolver() {}

    public static MessageType resolve(JSONObject action) {
        String type = action.optString(JsonConstants.FIELD_TYPE);

        JSONObject data = action.optJSONObject(JsonConstants.OBJECT_DATA);

        return switch (type) {

            case JsonConstants.EVENT_CREATE_CARD ->
                    MessageType.CARD_CREATED;

            case JsonConstants.EVENT_COMMENT_CARD ->
                    MessageType.CARD_COMMENTED;

            case JsonConstants.EVENT_ADDED_LABEL_TO_CARD ->
                    MessageType.CARD_ADDED_LABEL;

            case JsonConstants.EVENT_REMOVED_LABEL_FROM_CARD ->
                    MessageType.CARD_REMOVED_LABEL;

            case JsonConstants.EVENT_ADD_MEMBER_TO_CARD ->
                    MessageType.CARD_MEMBER_ASSIGNED;

            case JsonConstants.EVENT_REMOVE_MEMBER_FROM_CARD ->
                    MessageType.CARD_MEMBER_UNASSIGNED;

            case JsonConstants.EVENT_UPDATE_CARD ->
                    resolveUpdate(data);

            default ->
                    MessageType.INVALID;
        };
    }

    private static MessageType resolveUpdate(JSONObject data) {
        if (data == null) {
            return MessageType.INVALID;
        }

        if (isCardMove(data)) {
            return MessageType.CARD_MOVED;
        }

        JSONObject old = data.optJSONObject("old");

        if (old == null) {
            return MessageType.INVALID;
        }

        if (old.has(JsonConstants.FIELD_DESC)) {
            return MessageType.CARD_DESCRIPTION_CHANGED;
        }

        if (old.has(JsonConstants.FIELD_NAME)) {
            return MessageType.CARD_TITLE_CHANGED;
        }

        return MessageType.INVALID;
    }

    private static boolean isCardMove(JSONObject data) {
        return data.has(JsonConstants.OBJECT_LIST_BEFORE)
                && data.has(JsonConstants.OBJECT_LIST_AFTER);
    }
}