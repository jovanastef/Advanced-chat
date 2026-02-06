package fink.chat.messages;

import java.io.Serializable;

public class JoinRoomRequest implements Serializable {
    public String roomId;
    public String userName;

    public JoinRoomRequest() {}

    public JoinRoomRequest(String roomId, String userName) {
        this.roomId = roomId;
        this.userName = userName;
    }
}