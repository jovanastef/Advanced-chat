package fink.chat.messages;

import java.io.Serializable;

public class EditRequest implements Serializable {
    public String msgId;
    public String newText;
    public String roomId; // za sobne poruke
    public String editor; // korisnicko ime

    public EditRequest() {}

    public EditRequest(String msgId, String newText, String roomId, String editor) {
        this.msgId = msgId;
        this.newText = newText;
        this.roomId = roomId;
        this.editor = editor;
    }
}