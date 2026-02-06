package fink.chat.messages;

import java.io.Serializable;

public class LoadHistoryRequest implements Serializable {
    public String roomId;
    public int page; // 0 = najnovije, 1 = prethodnih 10...

    public LoadHistoryRequest() {}

    public LoadHistoryRequest(String roomId, int page) {
        this.roomId = roomId;
        this.page = page;
    }
}