package vc.s908.stickerpipe_chat_sample.ui;

/**
 * Created by Dmitry Nezhydenko
 * Date 4/7/15
 */
public class ChatItem {
    public enum ChatItemType {
        MESSAGE_IN,
        STICKER_IN,
        MESSAGE_OUT,
        nextItemType, prevItemType, STICKER_OUT
    }

    private ChatItemType type;
    private String message;
    private long time;

    public ChatItem(ChatItemType type, String message, long time) {
        this.type = type;
        this.message = message;
        this.time = time;
    }

    public ChatItemType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }
}
