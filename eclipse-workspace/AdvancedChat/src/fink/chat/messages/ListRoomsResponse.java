package fink.chat.messages;

import java.io.Serializable;

public class ListRoomsResponse implements Serializable {
    public RoomInfo[] rooms;

    public ListRoomsResponse() {}
}