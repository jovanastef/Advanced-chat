package fink.chat;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import fink.chat.messages.ChatMessage;
import fink.chat.messages.InfoMessage;
import fink.chat.messages.KryoUtil;
import fink.chat.messages.ListUsers;
import fink.chat.messages.Login;
import fink.chat.messages.WhoRequest;
import fink.chat.messages.StoredMessage;
import fink.chat.messages.CreateRoomRequest;
import fink.chat.messages.CreateRoomResponse;
import fink.chat.messages.ListRoomsRequest;
import fink.chat.messages.ListRoomsResponse;
import fink.chat.messages.JoinRoomRequest;
import fink.chat.messages.JoinRoomResponse;
import fink.chat.messages.RoomInfo;


public class ChatServer implements Runnable{

	private volatile Thread thread = null;
	
	volatile boolean running = false;
	final Server server;
	final int portNumber;
	ConcurrentMap<String, Connection> userConnectionMap = new ConcurrentHashMap<String, Connection>();
	ConcurrentMap<Connection, String> connectionUserMap = new ConcurrentHashMap<Connection, String>();
	private final List<StoredMessage> allMessages = new CopyOnWriteArrayList<>();
	// Mapa svih soba: roomId -> RoomInfo
	private final Map<String, RoomInfo> rooms = new ConcurrentHashMap<>();
	// Ko je u kojoj sobi: userName -> Set<roomId>
	private final Map<String, Set<String>> userRooms = new ConcurrentHashMap<>();
	// Poruke po sobama: roomId -> List<StoredMessage>
	private final Map<String, List<StoredMessage>> roomMessages = new ConcurrentHashMap<>();
	
	public ChatServer(int portNumber) {
		this.server = new Server();
		
		this.portNumber = portNumber;
		KryoUtil.registerKryoClasses(server.getKryo());
		server.getKryo().register(CreateRoomRequest.class);
		server.getKryo().register(CreateRoomResponse.class);
		server.getKryo().register(ListRoomsRequest.class);
		server.getKryo().register(ListRoomsResponse.class);
		server.getKryo().register(JoinRoomRequest.class);
		server.getKryo().register(JoinRoomResponse.class);
		server.getKryo().register(RoomInfo.class);
		server.getKryo().register(StoredMessage[].class);
		registerListener();
	}
	private void registerListener() {
		server.addListener(new Listener() {
			public void received (Connection connection, Object object) {
				if (object instanceof Login) {
					Login login = (Login)object;
					newUserLogged(login, connection);
					connection.sendTCP(new InfoMessage("Hello "+login.getUserName()));
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				}
				
				if (object instanceof ChatMessage) {
				    ChatMessage chatMsg = (ChatMessage) object;

				    // Generisi ID ako nije postavljen
				    if (chatMsg.id == null || chatMsg.id.isEmpty()) {
				        chatMsg.id = java.util.UUID.randomUUID().toString();
				    }

				    // Ako nema roomId, dodeli default sobu (kasnije cu omoguciti vise soba)
				    if (chatMsg.roomId == null || chatMsg.roomId.isEmpty()) {
				        chatMsg.roomId = "default";
				    }

				    // Sacuvaj poruku na serveru
				    StoredMessage stored = new StoredMessage(
				        chatMsg.id,
				        chatMsg.user,
				        chatMsg.txt,
				        chatMsg.roomId
				    );
				    stored.replyToId = chatMsg.replyToId; // moze biti null
				    allMessages.add(stored);

				    // Debug ispis
				    System.out.println("Saved message ID: " + chatMsg.id + " | Room: " + chatMsg.roomId);

				    // Prosledi ostalima
				    broadcastChatMessage(chatMsg, connection);
				    return;
				}

				if (object instanceof WhoRequest) {
					ListUsers listUsers = new ListUsers(getAllUsers());
					connection.sendTCP(listUsers);
					return;
				}
				
				if (object instanceof CreateRoomRequest) {
				    CreateRoomRequest req = (CreateRoomRequest) object;
				    CreateRoomResponse res = new CreateRoomResponse();

				    if (req.roomName == null || req.roomName.trim().isEmpty()) {
				        res.success = false;
				        res.message = "Room name cannot be empty";
				        connection.sendTCP(res);
				        return;
				    }

				    String roomId = java.util.UUID.randomUUID().toString();
				    RoomInfo room = new RoomInfo(roomId, req.roomName.trim(), req.creator);
				    rooms.put(roomId, room);
				    roomMessages.put(roomId, new CopyOnWriteArrayList<>());

				    res.success = true;
				    res.roomId = roomId;
				    res.roomName = room.name;
				    connection.sendTCP(res);
				    System.out.println("Created room: " + room.name + " (" + roomId + ") by " + req.creator);
				    return;
				}

				if (object instanceof ListRoomsRequest) {
				    ListRoomsResponse res = new ListRoomsResponse();
				    res.rooms = rooms.values().toArray(new RoomInfo[0]);
				    connection.sendTCP(res);
				    return;
				}

				if (object instanceof JoinRoomRequest) {
				    JoinRoomRequest req = (JoinRoomRequest) object;
				    JoinRoomResponse res = new JoinRoomResponse();

				    RoomInfo room = rooms.get(req.roomId);
				    if (room == null) {
				        res.success = false;
				        res.message = "Room not found";
				        connection.sendTCP(res);
				        return;
				    }

				    // Zapamti da je korisnik u ovoj sobi
				    userRooms.computeIfAbsent(req.userName, k -> new ConcurrentSkipListSet<>()).add(req.roomId);

				    // Uzmi poslednjih 10 poruka iz sobe (obrnuto sortirano po vremenu)
				    List<StoredMessage> messages = roomMessages.get(req.roomId);
				    if (messages == null) messages = new ArrayList<>();

				    int fromIndex = Math.max(0, messages.size() - 10);
				    List<StoredMessage> last10 = new ArrayList<>(messages.subList(fromIndex, messages.size()));
				    res.last10Messages = last10.toArray(new StoredMessage[0]);
				    res.success = true;
				    connection.sendTCP(res);
				    System.out.println(req.userName + " joined room " + room.name);
				    return;
				}
			}
			
			public void disconnected(Connection connection) {
				String user = connectionUserMap.get(connection);
				connectionUserMap.remove(connection);
				userConnectionMap.remove(user);
				showTextToAll(user+" has disconnected!", connection);
			}
		});
	}
	
	String[] getAllUsers() {
		String[] users = new String[userConnectionMap.size()];
		int i=0;
		for (String user: userConnectionMap.keySet()) {
			users[i] = user;
			i++;
		}
		
		return users;
	}
	void newUserLogged(Login loginMessage, Connection conn) {
		userConnectionMap.put(loginMessage.getUserName(), conn);
		connectionUserMap.put(conn, loginMessage.getUserName());
		showTextToAll("User "+loginMessage.getUserName()+" has connected!", conn);
	}
	private void broadcastChatMessage(ChatMessage message, Connection exception) {
		for (Connection conn: userConnectionMap.values()) {
			if (conn.isConnected() && conn != exception)
				conn.sendTCP(message);
		}
	}
	private void showTextToAll(String txt, Connection exception) {
		System.out.println(txt);
		for (Connection conn: userConnectionMap.values()) {
			if (conn.isConnected() && conn != exception)
				conn.sendTCP(new InfoMessage(txt));
		}
	}
	public void start() throws IOException {
		server.start();
		server.bind(portNumber);
		
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
	@Override
	public void run() {
		running = true;
		
		while(running) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args) {
		
		if (args.length != 1) {
	        System.err.println("Usage: java -jar chatServer.jar <port number>");
	        System.out.println("Recommended port number is 54555");
	        System.exit(1);
	   }
	    
	   int portNumber = Integer.parseInt(args[0]);
	   try { 
		   ChatServer chatServer = new ChatServer(portNumber);
	   	   chatServer.start();
	   
			chatServer.thread.join();
	   } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	   } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	   }
	}
	
   
   
}
