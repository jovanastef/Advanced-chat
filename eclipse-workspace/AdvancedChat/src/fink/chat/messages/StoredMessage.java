package fink.chat.messages;

import java.io.Serializable;

public class StoredMessage implements Serializable {
    public String id;
    public String author;
    public String text;
    public long timestamp;
    public String roomId; // null za privatne poruke
    public boolean edited = false;
    public String replyToId; // null ako nije reply

    // Potreban prazan konstruktor za KryoNet
    public StoredMessage() {}

    public StoredMessage(String id, String author, String text, String roomId) {
        this.id = id;
        this.author = author;
        this.text = text;
        this.roomId = roomId;
        this.timestamp = System.currentTimeMillis();
    }
}