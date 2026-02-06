package fink.chat;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import fink.chat.messages.ChatMessage;
import fink.chat.messages.CreateRoomRequest;
import fink.chat.messages.CreateRoomResponse;
import fink.chat.messages.GroupMessage;
import fink.chat.messages.InfoMessage;
import fink.chat.messages.JoinRoomRequest;
import fink.chat.messages.JoinRoomResponse;
import fink.chat.messages.KryoUtil;
import fink.chat.messages.ListRoomsRequest;
import fink.chat.messages.ListRoomsResponse;
import fink.chat.messages.ListUsers;
import fink.chat.messages.LoadHistoryRequest;
import fink.chat.messages.LoadHistoryResponse;
import fink.chat.messages.Login;
import fink.chat.messages.PrivateMessage;
import fink.chat.messages.RoomInfo;
import fink.chat.messages.StoredMessage;
import fink.chat.messages.WhoRequest;

public class ChatClient implements Runnable{

	public static int DEFAULT_CLIENT_READ_BUFFER_SIZE = 1000000;
	public static int DEFAULT_CLIENT_WRITE_BUFFER_SIZE = 1000000;
	
	private volatile Thread thread = null;
	
	volatile boolean running = false;
	
	final Client client;
	final String hostName;
	final int portNumber;
	final String userName;
	
	private String currentRoomId = null;
	private int currentHistoryPage = 0;
	
	public ChatClient(String hostName, int portNumber, String userName) {
		this.client = new Client(DEFAULT_CLIENT_WRITE_BUFFER_SIZE, DEFAULT_CLIENT_READ_BUFFER_SIZE);
		
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.userName = userName;
		KryoUtil.registerKryoClasses(client.getKryo());
		client.getKryo().register(CreateRoomRequest.class);
		client.getKryo().register(CreateRoomResponse.class);
		client.getKryo().register(ListRoomsRequest.class);
		client.getKryo().register(ListRoomsResponse.class);
		client.getKryo().register(JoinRoomRequest.class);
		client.getKryo().register(JoinRoomResponse.class);
		client.getKryo().register(RoomInfo.class);
		client.getKryo().register(StoredMessage[].class);
		client.getKryo().register(LoadHistoryRequest.class);
		client.getKryo().register(LoadHistoryResponse.class);
		client.getKryo().register(PrivateMessage.class);
		client.getKryo().register(GroupMessage.class);
		registerListener();
	}
	private void registerListener() {
		client.addListener(new Listener() {
			public void connected (Connection connection) {
				Login loginMessage = new Login(userName);
				client.sendTCP(loginMessage);
			}
			
			public void received (Connection connection, Object object) {
				if (object instanceof ChatMessage) {
					ChatMessage chatMessage = (ChatMessage)object;
					showChatMessage(chatMessage);
					return;
				}

				if (object instanceof ListUsers) {
					ListUsers listUsers = (ListUsers)object;
					showOnlineUsers(listUsers.getUsers());
					return;
				}
				
				if (object instanceof InfoMessage) {
					InfoMessage message = (InfoMessage)object;
					showMessage("Server:"+message.getTxt());
					return;
				}
				
				if (object instanceof ChatMessage) {
					ChatMessage message = (ChatMessage)object;
					showMessage(message.getUser()+"r:"+message.getTxt());
					return;
				}
				
				if (object instanceof CreateRoomResponse) {
				    CreateRoomResponse res = (CreateRoomResponse) object;
				    if (res.success) {
				        System.out.println("Created room: " + res.roomName + " (ID: " + res.roomId + ")");
				    } else {
				        System.out.println("Failed to create room: " + res.message);
				    }
				    return;
				}

				if (object instanceof ListRoomsResponse) {
				    ListRoomsResponse res = (ListRoomsResponse) object;
				    if (res.rooms.length == 0) {
				        System.out.println("No rooms available.");
				    } else {
				        System.out.println("Available rooms:");
				        for (RoomInfo r : res.rooms) {
				            System.out.println("  [" + r.id + "] " + r.name + " (by " + r.creator + ")");
				        }
				    }
				    return;
				}

				if (object instanceof JoinRoomResponse) {
				    JoinRoomResponse res = (JoinRoomResponse) object;
				    if (res.success) {
				    	currentRoomId = res.roomId;
				    	System.out.println("Joined room: " + res.roomId);
				        System.out.println("Last 10 messages:");
				        if (res.last10Messages != null && res.last10Messages.length > 0) {
				            for (StoredMessage msg : res.last10Messages) {
				                System.out.println("[" + msg.id + "] " + msg.author + ": " + msg.text +
				                    (msg.edited ? " [Edited]" : ""));
				            }
				        } else {
				            System.out.println("(No messages yet)");
				        }
				    } else {
				        System.out.println("Failed to join room: " + res.message);
				    }
				    return;
				}
				
				if (object instanceof LoadHistoryResponse) {
				    LoadHistoryResponse res = (LoadHistoryResponse) object;
				    if (res.success && res.messages != null) {
				        if (res.messages.length == 0) {
				            System.out.println("No more messages.");
				            currentHistoryPage--; // vrati brojac
				        } else {
				            System.out.println("Historical messages:");
				            for (StoredMessage msg : res.messages) {
				                System.out.println("[" + msg.id + "] " + msg.author + ": " + msg.text +
				                    (msg.edited ? " [Edited]" : ""));
				            }
				            if (res.nextPage == -1) {
				                System.out.println("(End of history)");
				                currentHistoryPage--; // ne moze dalje
				            }
				        }
				    } else {
				        System.out.println("Failed to load history: " + (res.message != null ? res.message : "unknown error"));
				        currentHistoryPage--;
				    }
				    return;
				}
				
				if (object instanceof PrivateMessage) {
				    PrivateMessage pm = (PrivateMessage) object;
				    System.out.println("[PM from " + pm.fromUser + "] " + pm.text);
				    return;
				}

				if (object instanceof GroupMessage) {
				    GroupMessage gm = (GroupMessage) object;
				    System.out.println("[Group from " + gm.fromUser + "] " + gm.text);
				    return;
				}
			}
			
			public void disconnected(Connection connection) {
				
			}
		});
	}
	private void showChatMessage(ChatMessage chatMessage) {
		System.out.println(chatMessage.getUser()+":"+chatMessage.getTxt());
	}
	private void showMessage(String txt) {
		System.out.println(txt);
	}
	private void showOnlineUsers(String[] users) {
		System.out.print("Server:");
		for (int i=0; i<users.length; i++) {
			String user = users[i];
			System.out.print(user);
			System.out.printf((i==users.length-1?"\n":", "));
		}
	}
	public void start() throws IOException {
		client.start();
		connect();
		
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}
	public void stop() {
		Thread stopThread = thread;
		thread = null;
		running = false;
		if (stopThread != null)
			stopThread.interrupt();
	}
	
	public void connect() throws IOException {
		client.connect(1000, hostName, portNumber);
	}

	public void run() {
	    Scanner scanner = new Scanner(System.in);
	    while (running) {
	        String userInput = scanner.nextLine();
	        if (userInput == null || "BYE".equalsIgnoreCase(userInput)) {
	            running = false;
	        } else if ("WHO".equalsIgnoreCase(userInput)) {
	            client.sendTCP(new WhoRequest());
	        } else if (userInput.startsWith("/create ")) {
	            String roomName = userInput.substring(8).trim();
	            client.sendTCP(new CreateRoomRequest(roomName, userName));
	        } else if ("/listrooms".equalsIgnoreCase(userInput)) {
	            client.sendTCP(new ListRoomsRequest());
	        } else if (userInput.startsWith("/join ")) {
	            String roomId = userInput.substring(6).trim();
	            client.sendTCP(new JoinRoomRequest(roomId, userName));
	        } else if ("/history".equalsIgnoreCase(userInput)) {
	            if (currentRoomId == null) {
	                System.out.println("You are not in any room!");
	            } else {
	                client.sendTCP(new LoadHistoryRequest(currentRoomId, currentHistoryPage));
	                currentHistoryPage++; // povecaj za sledeci poziv
	            }
	        } else if (userInput.startsWith("/pm ")) {
	            String[] parts = userInput.substring(4).trim().split(" ", 2);
	            if (parts.length < 2) {
	                System.out.println("Usage: /pm <username> <message>");
	            } else {
	                String toUser = parts[0];
	                String text = parts[1];
	                client.sendTCP(new PrivateMessage(userName, toUser, text));
	            }
	        } else if (userInput.startsWith("/group ")) {
	            String rest = userInput.substring(7).trim();
	            int spaceIndex = rest.indexOf(' ');
	            if (spaceIndex == -1) {
	                System.out.println("Usage: /group <user1,user2,...> <message>");
	            } else {
	                String userList = rest.substring(0, spaceIndex);
	                String text = rest.substring(spaceIndex + 1);
	                String[] recipients = userList.split(",");
	                for (int i = 0; i < recipients.length; i++) {
	                    recipients[i] = recipients[i].trim();
	                }
	                client.sendTCP(new GroupMessage(userName, recipients, text));
	            }
	            
	        } else {
	            // Obicna poruka, saljemo u trenutnu sobu
	            if (currentRoomId == null) {
	                System.out.println("You are not in any room! Use /join <roomId>");
	            } else {
	                ChatMessage message = new ChatMessage(userName, userInput);
	                message.roomId = currentRoomId;
	                client.sendTCP(message);
	            }
	        }
	    }
	    scanner.close();
	    client.stop();
	}
	public static void main(String[] args) {
		if (args.length != 3) {
		
            System.err.println(
                "Usage: java -jar chatClient.jar <host name> <port number> <username>");
            System.out.println("Recommended port number is 54555");
            System.exit(1);
        }
 
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        String userName = args[2];
        
        try{
        	ChatClient chatClient = new ChatClient(hostName, portNumber, userName);
        	chatClient.start();
        }catch(IOException e) {
        	e.printStackTrace();
        	System.err.println("Error:"+e.getMessage());
        	System.exit(-1);
        }
	}
}
