package tij.bot.trello2discord.common;

public enum MessageType {
    CARD_CREATED(
            "📥 New Card Created",
            "%s",
            MessageField.of("List", "%s")
    ),
    CARD_COMMENTED(
            "💬 New Comment on Card",
            "\n\n**Comment:** %s"
    ),
    CARD_MOVED(
            "🚚 Card Moved",
            "%s",
            MessageField.of("From", "%s"),
            MessageField.of("To", "%s")
    ),
    CARD_ADDED_LABEL(
            "🏷️ Added Label To Card",
            "%s",
            MessageField.of("Label", "%s")
    ),
    CARD_REMOVED_LABEL(
            "🏷️ Removed Label From Card",
            "%s",
            MessageField.of("Label", "%s")
    );

    private final String titleMessage;
    private final String bodyFormat;
    private final MessageField[] fields;

    MessageType(String titleMessage, String bodyFormat, MessageField... fields) {
        this.titleMessage = titleMessage;
        this.bodyFormat = bodyFormat;
        this.fields = fields;
    }

    public String getTitleMessage() {
        return titleMessage;
    }

    public String getBodyFormat(Object... formatObjects) {
        return bodyFormat.formatted(formatObjects);
    }

    public MessageField[] getFields() {
        return fields;
    }
}
