package fink.chat.messages;

import java.io.Serializable;

public class JoinRoomResponse implements Serializable {
    public StoredMessage[] last10Messages;
    public boolean success;
    public String message;
    public String roomId;

    public JoinRoomResponse() {}
}