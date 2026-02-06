package fink.chat.messages;

import java.io.Serializable;

public class GroupMessage implements Serializable {
    public String fromUser;
    public String[] recipients;
    public String text;
    public String id;

    public GroupMessage() {}

    public GroupMessage(String fromUser, String[] recipients, String text) {
        this.fromUser = fromUser;
        this.recipients = recipients;
        this.text = text;
        this.id = java.util.UUID.randomUUID().toString();
    }
}