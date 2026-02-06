package fink.chat.messages;

import java.io.Serializable;

public class RoomInfo implements Serializable {
    public String id;
    public String name;
    public String creator;

    public RoomInfo() {}

    public RoomInfo(String id, String name, String creator) {
        this.id = id;
        this.name = name;
        this.creator = creator;
    }
}