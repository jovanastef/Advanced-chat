package fink.chat.messages;

import java.io.Serializable;

public class LoadHistoryResponse implements Serializable {
    public StoredMessage[] messages;
    public int nextPage; // -1 ako nema vise
    public boolean success;
    public String message;

    public LoadHistoryResponse() {}
}