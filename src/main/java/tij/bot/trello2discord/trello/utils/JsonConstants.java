package tij.bot.trello2discord.trello.utils;

public final class JsonConstants {
    public static final String OBJECT_ACTION = "action";
    public static final String OBJECT_MEMBER_CREATOR = "memberCreator";
    public static final String OBJECT_DATA = "data";
    public static final String OBJECT_BOARD = "board";
    public static final String OBJECT_CARD = "card";
    public static final String OBJECT_LABEL = "label";
    public static final String OBJECT_LIST = "list";
    public static final String OBJECT_LIST_BEFORE = "listBefore";
    public static final String OBJECT_LIST_AFTER = "listAfter";


    public static final String FIELD_TYPE = "type";
    public static final String FIELD_FULL_NAME = "fullName";
    public static final String FIELD_AVATAR_URL = "avatarUrl";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TEXT = "text";


    public static final String EVENT_CREATE_CARD = "createCard";
    public static final String EVENT_COMMENT_CARD = "commentCard";
    public static final String EVENT_UPDATE_CARD = "updateCard";
    public static final String EVENT_ADDED_LABEL_TO_CARD = "addLabelToCard";
    public static final String EVENT_REMOVED_LABEL_FROM_CARD = "removeLabelFromCard";


    private JsonConstants() {}
}
