package fink.chat.messages;

import java.io.Serializable;

public class CreateRoomResponse implements Serializable {
    public String roomId;
    public String roomName;
    public boolean success;
    public String message; // opcionalna greska

    public CreateRoomResponse() {}
}