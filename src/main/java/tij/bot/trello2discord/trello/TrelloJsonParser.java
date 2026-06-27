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
        String cardName = cardObj != null
                ? cardObj.optString(JsonConstants.FIELD_NAME, "N/A")
                : "N/A";

        String boardName = data.optJSONObject(JsonConstants.OBJECT_BOARD) != null
                ? data.getJSONObject(JsonConstants.OBJECT_BOARD)
                .optString(JsonConstants.FIELD_NAME, "Unknown Board")
                : "Unknown Board";

        return switch (actionType) {

            case JsonConstants.EVENT_CREATE_CARD -> emit(
                    MessageType.CARD_CREATED,
                    boardName,
                    memberName,
                    avatarUrl,
                    builder -> {
                        builder.body(buildCardString(cardName));

                        JSONObject list = data.optJSONObject(JsonConstants.OBJECT_LIST);
                        if (list != null && builder.type().getFields().length > 0) {
                            builder.field(
                                    0,
                                    list.optString(JsonConstants.FIELD_NAME, "")
                            );
                        }
                    }
            );

            case JsonConstants.EVENT_COMMENT_CARD -> emit(
                    MessageType.CARD_COMMENTED,
                    boardName,
                    memberName,
                    avatarUrl,
                    builder -> builder.body(
                            buildCardString(cardName)
                                    + builder.type().getBodyFormat(data.optString(JsonConstants.FIELD_TEXT, ""))
                    )
            );

            case JsonConstants.EVENT_ADDED_LABEL_TO_CARD,
                 JsonConstants.EVENT_REMOVED_LABEL_FROM_CARD -> {

                JSONObject label = data.optJSONObject(JsonConstants.OBJECT_LABEL);
                if (label == null) yield Optional.empty();

                MessageType type = JsonConstants.EVENT_ADDED_LABEL_TO_CARD.equals(actionType)
                        ? MessageType.CARD_ADDED_LABEL
                        : MessageType.CARD_REMOVED_LABEL;

                yield emit(
                        type,
                        boardName,
                        memberName,
                        avatarUrl,
                        builder -> builder
                                .body(buildCardString(cardName))
                                .field(0, label.optString(JsonConstants.FIELD_NAME, ""))
                );
            }

            case JsonConstants.EVENT_UPDATE_CARD -> {

                JSONObject beforeList = data.optJSONObject(JsonConstants.OBJECT_LIST_BEFORE);
                JSONObject afterList = data.optJSONObject(JsonConstants.OBJECT_LIST_AFTER);
                JSONObject old = data.optJSONObject("old");

                if (beforeList != null && afterList != null) {
                    yield emit(
                            MessageType.CARD_MOVED,
                            boardName,
                            memberName,
                            avatarUrl,
                            builder -> builder
                                    .body(buildCardString(cardName))
                                    .field(0, beforeList.optString(JsonConstants.FIELD_NAME, ""))
                                    .field(1, afterList.optString(JsonConstants.FIELD_NAME, ""))
                    );
                }

                if (old != null && old.has(JsonConstants.FIELD_DESC)) {
                    yield emit(
                            MessageType.CARD_DESCRIPTION_CHANGED,
                            boardName,
                            memberName,
                            avatarUrl,
                            builder -> builder
                                    .field(0, old.optString(JsonConstants.FIELD_DESC, ""))
                                    .field(1, data.getJSONObject(JsonConstants.OBJECT_CARD)
                                            .optString(JsonConstants.FIELD_DESC, ""))
                    );
                }

                if (old != null && old.has(JsonConstants.FIELD_NAME)) {
                    yield emit(
                            MessageType.CARD_TITLE_CHANGED,
                            boardName,
                            memberName,
                            avatarUrl,
                            builder -> builder
                                    .field(0, old.optString(JsonConstants.FIELD_NAME, ""))
                                    .field(1, data.getJSONObject(JsonConstants.OBJECT_CARD)
                                            .optString(JsonConstants.FIELD_NAME, ""))
                    );
                }

                yield Optional.empty();
            }

            default -> Optional.empty();
        };
    }

    private interface BuilderFn {
        void apply(BuilderWrapper builder);
    }

    private static Optional<Message> emit(
            MessageType type,
            String board,
            String author,
            String avatar,
            BuilderFn fn
    ) {
        BuilderWrapper wrapper = new BuilderWrapper(type, board, author);
        fn.apply(wrapper);
        return Optional.of(wrapper.build(avatar));
    }

    private static class BuilderWrapper {
        private final MessageType type;
        private final Message.Builder builder;

        BuilderWrapper(MessageType type, String board, String author) {
            this.type = type;
            this.builder = new Message.Builder(type, board, author)
                    .title(type.getTitleMessage());
        }

        BuilderWrapper body(String body) {
            builder.body(body);
            return this;
        }

        BuilderWrapper field(int index, String value) {
            if (type.getFields().length > index) {
                var f = type.getFields()[index];
                builder.addField(f.title(), f.bodyFormat(value));
            }
            return this;
        }

        MessageType type() {
            return type;
        }

        Message build(String avatar) {
            return builder.authorAvatarUrl(avatar).build();
        }
    }

    private static String buildCardString(String cardName) {
        return "**Card:** %s".formatted(cardName);
    }
}