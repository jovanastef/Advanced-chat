package fink.chat.messages;

import java.io.Serializable;

public class ChatMessage implements Serializable {
	public String user;
	public String txt;
	public String id;
	public String roomId;
	public String replyToId;
	
	protected ChatMessage() {
		
	}
	public ChatMessage(String user, String txt) {
		this.user = user;
		this.txt = txt;
	}
	
	public ChatMessage(String user, String txt, String id, String roomId, String replyToId) {
        this.user = user;
        this.txt = txt;
        this.id = id;
        this.roomId = roomId;
        this.replyToId = replyToId;
    }

	public String getUser() {
		return user;
	}

	public String getTxt() {
		return txt;
	}
	
	public String getId() {
        return id;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getReplyToId() {
        return replyToId;
    }
    
    
}
