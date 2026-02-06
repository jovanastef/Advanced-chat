package fink.chat.messages;

import java.io.Serializable;

public class PrivateMessage implements Serializable {
    public String fromUser;
    public String toUser;
    public String text;
    public String id;

    public PrivateMessage() {}

    public PrivateMessage(String fromUser, String toUser, String text) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.text = text;
        this.id = java.util.UUID.randomUUID().toString();
    }
}