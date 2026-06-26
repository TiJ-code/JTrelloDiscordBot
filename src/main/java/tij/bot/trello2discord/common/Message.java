package tij.bot.trello2discord.common;

import java.util.HashMap;
import java.util.Map;

public class Message {
    private final MessageType type;
    private final String authorName;
    private final String authorAvatarUrl;
    private final String boardName;
    private final String title;
    private final String body;
    private final Map<String, String> fields;

    private Message(Builder builder) {
        this.type = builder.type;
        this.authorName = builder.authorName;
        this.authorAvatarUrl = builder.authorAvatarUrl;
        this.boardName = builder.boardName;
        this.title = builder.title;
        this.body = builder.body;
        this.fields = builder.fields;
    }

    public MessageType getType() {
        return type;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public String getBoardName() {
        return boardName;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public static class Builder {
        private final Map<String, String> fields = new HashMap<>();
        private final MessageType type;
        private final String authorName;
        private final String boardName;
        private String authorAvatarUrl;
        private String title;
        private String body;

        public Builder(MessageType type, String boardName, String authorName) {
            this.type = type;
            this.boardName = boardName;
            this.authorName = authorName;
        }

        public Builder authorAvatarUrl(String url) {
            this.authorAvatarUrl = url;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder addField(String key, String value) {
            this.fields.put(key, value);
            return this;
        }

        public Message build() {
            return new Message(this);
        }
    }
}
