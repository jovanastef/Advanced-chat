package fink.chat.messages;

public class Login {
	String userName;
	
	protected Login() {
		
	}
	public Login(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}	
}
