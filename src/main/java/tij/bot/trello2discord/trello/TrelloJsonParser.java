package tij.bot.trello2discord.trello;

import org.json.JSONObject;
import tij.bot.trello2discord.common.Message;
import tij.bot.trello2discord.common.MessageType;
import tij.bot.trello2discord.trello.utils.JsonConstants;

import java.util.Optional;

public class TrelloJsonParser {
    public static Optional<Message> parse(JSONObject json) {
        if (!json.has(JsonConstants.OBJECT_ACTION))
            return Optional.empty();

        JSONObject action = json.getJSONObject(JsonConstants.OBJECT_ACTION);
        String actionType = action.getString(JsonConstants.FIELD_TYPE);
        String memberName = action.getJSONObject(JsonConstants.OBJECT_MEMBER_CREATOR).getString(JsonConstants.FIELD_FULL_NAME);
        String avatarUrl = action.getJSONObject(JsonConstants.OBJECT_MEMBER_CREATOR).optString(JsonConstants.FIELD_AVATAR_URL, "") + "/50.png";

        JSONObject data = action.getJSONObject(JsonConstants.OBJECT_DATA);
        String cardName = data.has(JsonConstants.OBJECT_CARD) ? data.getJSONObject(JsonConstants.OBJECT_CARD).getString(JsonConstants.FIELD_NAME) : "N/A";
        String boardName = data.getJSONObject(JsonConstants.OBJECT_BOARD).getString(JsonConstants.FIELD_NAME);

        MessageType type;
        Message.Builder builder;

        switch (actionType) {
            case JsonConstants.EVENT_CREATE_CARD:
                type = MessageType.CARD_CREATED;
                builder = new Message.Builder(type, boardName, memberName)
                        .title(type.getTitleMessage())
                        .body(buildCardString(cardName));
                if (data.has(JsonConstants.OBJECT_LIST)) {
                    builder.addField(
                          type.getFields()[0].title(),
                          type.getFields()[0].bodyFormat(
                                  data.getJSONObject(JsonConstants.OBJECT_LIST).getString(JsonConstants.FIELD_NAME)
                          )
                    );
                }
                break;

            case JsonConstants.EVENT_COMMENT_CARD:
                type = MessageType.CARD_COMMENTED;
                builder = new Message.Builder(type, boardName, memberName)
                        .title(type.getTitleMessage())
                        .body(buildCardString(cardName) + type.getBodyFormat(data.getString(JsonConstants.FIELD_TEXT)));
                break;

            case JsonConstants.EVENT_UPDATE_CARD:
                if (data.has(JsonConstants.OBJECT_LIST_AFTER) && data.has(JsonConstants.OBJECT_LIST_BEFORE)) {
                    type = MessageType.CARD_MOVED;
                    builder = new Message.Builder(type, boardName, memberName)
                            .title(type.getTitleMessage())
                            .body(buildCardString(cardName))
                            .addField(
                                    type.getFields()[0].title(),
                                    type.getFields()[0].bodyFormat(
                                            data.getJSONObject(JsonConstants.OBJECT_LIST_BEFORE).getString(JsonConstants.FIELD_NAME)
                                    )
                            )
                            .addField(
                                    type.getFields()[1].title(),
                                    type.getFields()[1].bodyFormat(
                                            data.getJSONObject(JsonConstants.OBJECT_LIST_AFTER).getString(JsonConstants.FIELD_NAME)
                                    )
                            );
                } else {
                    type = MessageType.GENERIC_UPDATE;
                    builder = new Message.Builder(type, boardName, memberName)
                            .title(type.getTitleMessage())
                            .body(buildCardString(cardName));
                }
                break;

            default:
                return Optional.empty();
        }

        return Optional.of(builder.authorAvatarUrl(avatarUrl).build());
    }

    private static String buildCardString(String cardName) {
        return "**Card:** %s".formatted(cardName);
    }
}
