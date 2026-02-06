package fink.chat.messages;

public class ChatMessage {
	String user;
	String txt;
	
	protected ChatMessage() {
		
	}
	public ChatMessage(String user, String txt) {
		this.user = user;
		this.txt = txt;
	}

	public String getUser() {
		return user;
	}

	public String getTxt() {
		return txt;
	}
	
	
}
