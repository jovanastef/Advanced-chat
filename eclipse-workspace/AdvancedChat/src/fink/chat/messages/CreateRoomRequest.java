package fink.chat.messages;

import java.io.Serializable;

public class CreateRoomRequest implements Serializable {
    public String roomName;
    public String creator; // korisnicko ime

    public CreateRoomRequest() {}

    public CreateRoomRequest(String roomName, String creator) {
        this.roomName = roomName;
        this.creator = creator;
    }
}