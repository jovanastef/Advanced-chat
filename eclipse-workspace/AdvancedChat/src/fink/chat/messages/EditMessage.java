package fink.chat.messages;

import java.io.Serializable;

public class EditMessage implements Serializable {
    public String msgId;
    public String newText;
    public String roomId;

    public EditMessage() {}

    public EditMessage(String msgId, String newText, String roomId) {
        this.msgId = msgId;
        this.newText = newText;
        this.roomId = roomId;
    }
}