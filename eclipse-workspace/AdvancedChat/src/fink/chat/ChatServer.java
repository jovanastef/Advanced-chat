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
import fink.chat.messages.PrivateMessage;
import fink.chat.messages.WhoRequest;
import fink.chat.messages.StoredMessage;
import fink.chat.messages.CreateRoomRequest;
import fink.chat.messages.CreateRoomResponse;
import fink.chat.messages.EditMessage;
import fink.chat.messages.EditRequest;
import fink.chat.messages.GroupMessage;
import fink.chat.messages.ListRoomsRequest;
import fink.chat.messages.ListRoomsResponse;
import fink.chat.messages.JoinRoomRequest;
import fink.chat.messages.JoinRoomResponse;
import fink.chat.messages.RoomInfo;
import fink.chat.messages.LoadHistoryRequest;
import fink.chat.messages.LoadHistoryResponse;


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
		server.getKryo().register(StoredMessage.class);
		server.getKryo().register(StoredMessage[].class);
		server.getKryo().register(LoadHistoryRequest.class);
		server.getKryo().register(LoadHistoryResponse.class);
		server.getKryo().register(PrivateMessage.class);
		server.getKryo().register(GroupMessage.class);
		server.getKryo().register(EditRequest.class);
		server.getKryo().register(EditMessage.class);
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

				    // Proveri da li je roomId postavljen
				    if (chatMsg.roomId == null || chatMsg.roomId.isEmpty()) {
				        // Odbaci poruku ako nema sobe
				        System.err.println("Dropped message without roomId from " + chatMsg.user);
				        return;
				    }

				    // Sacuvaj poruku u globalnoj listi (opciono)
				    StoredMessage storedGlobal = new StoredMessage(
				        chatMsg.id,
				        chatMsg.user,
				        chatMsg.txt,
				        chatMsg.roomId
				    );
				    storedGlobal.replyToId = chatMsg.replyToId;
				    allMessages.add(storedGlobal);

				    // Sacuvaj poruku u istoriji sobe
				    List<StoredMessage> roomMsgList = roomMessages.get(chatMsg.roomId);
				    if (roomMsgList != null) {
				        StoredMessage storedForRoom = new StoredMessage(
				            chatMsg.id,
				            chatMsg.user,
				            chatMsg.txt,
				            chatMsg.roomId
				        );
				        storedForRoom.replyToId = chatMsg.replyToId;
				        roomMsgList.add(storedForRoom);
				    }

				    // Prosledi poruku SVIM clanovima sobe, UKLJUCUJUCI posiljaoca (zbog id poruke)
				    Set<String> usersInRoom = new HashSet<>();
				    for (Map.Entry<String, Set<String>> entry : userRooms.entrySet()) {
				        if (entry.getValue().contains(chatMsg.roomId)) {
				            usersInRoom.add(entry.getKey());
				        }
				    }

				    for (String user : usersInRoom) {
				        Connection conn = userConnectionMap.get(user);
				        if (conn != null && conn.isConnected()) {
				            conn.sendTCP(chatMsg);
				        }
				    }

				    System.out.println("Message sent to room " + chatMsg.roomId + " from " + chatMsg.user);
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
				    res.roomId = req.roomId;
				    connection.sendTCP(res);
				    System.out.println(req.userName + " joined room " + room.name);
				    return;
				}
				
				if (object instanceof LoadHistoryRequest) {
				    LoadHistoryRequest req = (LoadHistoryRequest) object;
				    LoadHistoryResponse res = new LoadHistoryResponse();

				    if (req.roomId == null || req.page < 0) {
				        res.success = false;
				        res.message = "Invalid request";
				        connection.sendTCP(res);
				        return;
				    }

				    List<StoredMessage> allMsgs = roomMessages.get(req.roomId);
				    if (allMsgs == null || allMsgs.isEmpty()) {
				        res.messages = new StoredMessage[0];
				        res.nextPage = -1;
				        res.success = true;
				        connection.sendTCP(res);
				        return;
				    }

				    // Sortiramo po timestampu opadajuce (najnovije prvo)
				    List<StoredMessage> sorted = new ArrayList<>(allMsgs);
				    sorted.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

				    int total = sorted.size();
				    int fromIndex = req.page * 10;
				    int toIndex = Math.min(fromIndex + 10, total);

				    if (fromIndex >= total) {
				        res.messages = new StoredMessage[0];
				        res.nextPage = -1;
				    } else {
				        List<StoredMessage> pageMessages = sorted.subList(fromIndex, toIndex);
				        res.messages = pageMessages.toArray(new StoredMessage[0]);
				        res.nextPage = (toIndex < total) ? req.page + 1 : -1;
				    }

				    res.success = true;
				    connection.sendTCP(res);
				    return;
				}
				
				if (object instanceof PrivateMessage) {
				    PrivateMessage pm = (PrivateMessage) object;

				    // Sacuvaj poruku (opciono)
				    StoredMessage stored = new StoredMessage(pm.id, pm.fromUser, pm.text, null);
				    stored.replyToId = null;
				    allMessages.add(stored);

				    // Prosledi primaocu
				    Connection toConn = userConnectionMap.get(pm.toUser);
				    if (toConn != null && toConn.isConnected()) {
				        toConn.sendTCP(pm);
				        System.out.println("PM from " + pm.fromUser + " to " + pm.toUser);
				    } else {
				        // Opciono: posalji gresku posiljaocu
				        InfoMessage error = new InfoMessage("User " + pm.toUser + " is not online.");
				        connection.sendTCP(error);
				    }
				    return;
				}
				
				if (object instanceof GroupMessage) {
				    GroupMessage gm = (GroupMessage) object;

				    // Sacuvaj poruku (opciono)
				    StoredMessage stored = new StoredMessage(gm.id, gm.fromUser, gm.text, null);
				    allMessages.add(stored);

				    // Prosledi svim online primaocima
				    for (String recipient : gm.recipients) {
				    	System.out.println("Sending group msg to: '" + recipient + "'"); //debug
				        Connection conn = userConnectionMap.get(recipient);
				        if (conn != null && conn.isConnected()) {
				            conn.sendTCP(gm);
				        }
				    }
				    System.out.println("Group message from " + gm.fromUser + " to " + String.join(",", gm.recipients));
				    return;
				}
				
				if (object instanceof EditRequest) {
				    EditRequest req = (EditRequest) object;
				    EditMessage response = null;

				    // Pronadji poruku u globalnoj listi
				    StoredMessage target = null;
				    for (StoredMessage msg : allMessages) {
				        if (msg.id.equals(req.msgId)) {
				            target = msg;
				            break;
				        }
				    }

				    if (target == null) {
				        connection.sendTCP(new InfoMessage("Message not found."));
				        return;
				    }

				    // Proveri da li je autor isti kao editor
				    if (!target.author.equals(req.editor)) {
				        connection.sendTCP(new InfoMessage("You can only edit your own messages."));
				        return;
				    }

				    // Azuriraj poruku
				    target.text = req.newText;
				    target.edited = true;

				    // Posalji obavestenje svim clanovima sobe
				    if (req.roomId != null) {
				        Set<String> usersInRoom = new HashSet<>();
				        for (Map.Entry<String, Set<String>> entry : userRooms.entrySet()) {
				            if (entry.getValue().contains(req.roomId)) {
				                usersInRoom.add(entry.getKey());
				            }
				        }

				        response = new EditMessage(req.msgId, req.newText, req.roomId);
				        for (String user : usersInRoom) {
				            Connection conn = userConnectionMap.get(user);
				            if (conn != null && conn.isConnected()) {
				                conn.sendTCP(response);
				            }
				        }
				    }

				    System.out.println("Message " + req.msgId + " edited by " + req.editor);
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
