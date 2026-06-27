package tij.bot.trello2discord.trello;

import org.json.JSONObject;
import tij.bot.trello2discord.common.Message;
import tij.bot.trello2discord.common.MessageType;
import tij.bot.trello2discord.trello.utils.JsonConstants;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TrelloJsonParser {
    public static Optional<Message> parse(JSONObject json) {
        if (!json.has(JsonConstants.OBJECT_ACTION))
            return Optional.empty();

        JSONObject action = json.getJSONObject(JsonConstants.OBJECT_ACTION);
        String actionType = action.optString(JsonConstants.FIELD_TYPE, "");

        JSONObject member = action.optJSONObject(JsonConstants.OBJECT_MEMBER_CREATOR);
        String memberName = member != null
                ? member.optString(JsonConstants.FIELD_FULL_NAME, "Unknown")
                : "Unknown";

        String avatarUrl = member != null
                ? member.optString(JsonConstants.FIELD_AVATAR_URL, "") + "/50.png"
                : "";

        JSONObject data = action.optJSONObject(JsonConstants.OBJECT_DATA);
        if (data == null) return Optional.empty();

        JSONObject cardObj = data.optJSONObject(JsonConstants.OBJECT_CARD);
        String cardId = cardObj != null ? cardObj.optString("id", "unknown") : "unknown";
        String cardName = cardObj != null ? cardObj.optString(JsonConstants.FIELD_NAME, "N/A") : "N/A";

        String boardName = data.optJSONObject(JsonConstants.OBJECT_BOARD) != null
                ? data.getJSONObject(JsonConstants.OBJECT_BOARD).optString(JsonConstants.FIELD_NAME, "Unknown Board")
                : "Unknown Board";

        return switch (actionType) {

            case JsonConstants.EVENT_CREATE_CARD -> {
                MessageType type = MessageType.CARD_CREATED;

                Message.Builder builder = new Message.Builder(type, boardName, memberName)
                        .title(type.getTitleMessage())
                        .body(buildCardString(cardName));

                JSONObject list = data.optJSONObject(JsonConstants.OBJECT_LIST);
                if (list != null && type.getFields().length > 0) {
                    builder.addField(
                            type.getFields()[0].title(),
                            type.getFields()[0].bodyFormat(list.optString(JsonConstants.FIELD_NAME, ""))
                    );
                }

                yield Optional.of(builder.authorAvatarUrl(avatarUrl).build());
            }

            case JsonConstants.EVENT_COMMENT_CARD -> {
                MessageType type = MessageType.CARD_COMMENTED;

                String comment = data.optString(JsonConstants.FIELD_TEXT, "");

                Message.Builder builder = new Message.Builder(type, boardName, memberName)
                        .title(type.getTitleMessage())
                        .body(buildCardString(cardName) + type.getBodyFormat(comment));

                yield Optional.of(builder.authorAvatarUrl(avatarUrl).build());
            }

            case JsonConstants.EVENT_ADDED_LABEL_TO_CARD,
                 JsonConstants.EVENT_REMOVED_LABEL_FROM_CARD -> {
                JSONObject label = data.optJSONObject(JsonConstants.OBJECT_LABEL);
                if (label == null) yield Optional.empty();

                MessageType type = JsonConstants.EVENT_ADDED_LABEL_TO_CARD.equals(actionType)
                        ? MessageType.CARD_ADDED_LABEL
                        : MessageType.CARD_REMOVED_LABEL;

                Message.Builder builder = new Message.Builder(type, boardName, memberName)
                        .title(type.getTitleMessage())
                        .body(type.getBodyFormat(buildCardString(cardName)))
                        .addField(
                                type.getFields()[0].title(),
                                type.getFields()[0].bodyFormat(label.optString(JsonConstants.FIELD_NAME, ""))
                        );

                yield Optional.of(builder.authorAvatarUrl(avatarUrl).build());
            }

            case JsonConstants.EVENT_UPDATE_CARD -> {
                JSONObject before = data.optJSONObject(JsonConstants.OBJECT_LIST_BEFORE);
                JSONObject after = data.optJSONObject(JsonConstants.OBJECT_LIST_AFTER);

                if (before == null || after == null) {
                    yield Optional.empty();
                }

                MessageType type = MessageType.CARD_MOVED;

                Message.Builder builder = new Message.Builder(type, boardName, memberName)
                        .title(type.getTitleMessage())
                        .body(buildCardString(cardName))
                        .addField(
                                type.getFields()[0].title(),
                                type.getFields()[0].bodyFormat(before.optString(JsonConstants.FIELD_NAME, ""))
                        )
                        .addField(
                                type.getFields()[1].title(),
                                type.getFields()[1].bodyFormat(after.optString(JsonConstants.FIELD_NAME, ""))
                        );

                yield Optional.of(builder.authorAvatarUrl(avatarUrl).build());
            }

            default -> Optional.empty();
        };
    }

    private static String buildCardString(String cardName) {
        return "**Card:** %s".formatted(cardName);
    }
}