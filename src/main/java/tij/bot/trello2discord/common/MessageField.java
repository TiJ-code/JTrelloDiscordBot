package tij.bot.trello2discord.common;

public record MessageField(String title, String bodyFormat, boolean inline) {
    public static MessageField of(String title, String bodyFormat) {
        return of(title, bodyFormat, false);
    }

    public static MessageField of(String title, String bodyFormat, boolean inline) {
        return new MessageField(title, bodyFormat, inline);
    }

    public String bodyFormat(Object... formatObjects) {
        return bodyFormat().formatted(formatObjects);
    }
}
