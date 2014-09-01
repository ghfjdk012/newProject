package chat4;

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {
	private ServerSocket serverSocket;
	private List threadPool;

	public ChatServer(int port) {
		threadPool = new ArrayList(10);

		try {
			serverSocket = new ServerSocket(port);
			System.out.println(port + " ��Ʈ���� ������ ��ٸ��ϴ�.");
			start();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println("ä�� ������ ����˴ϴ�.");
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException ioe) {	}
			}
		}
	}
	
	public void start() throws IOException {
		Socket socket;
		Thread serverThread;
		while(true) {
			socket = serverSocket.accept();
			System.out.println(socket.getInetAddress() + " ���� �����߽��ϴ�.");
			serverThread = new ServerThread(this, socket);
			addThread(serverThread);	
			serverThread.start();
		}
	}
	
	public void broadCast(String message) {
		ServerThread serverThread;
		for (int i = 0; i < threadPool.size() ; i++) {
			serverThread = (ServerThread)threadPool.get(i);
			serverThread.sendMessage(message);
		}
	}

	public void addThread(Thread aThread) {
		threadPool.add(aThread);
	}

	public void removeThread(Thread aThread) {
		threadPool.remove(aThread);
	}

	public static void main(String[] args) {
		ChatServer server = new ChatServer(5432);
	}
}

class ServerThread extends Thread {
	private ChatServer server;
	private Socket socket;
	private BufferedReader socketReader;
	private PrintWriter socketWriter;
	private String userName;	

	public ServerThread(ChatServer server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}

	public void run() {
		String message = null;
		try {
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			socketWriter = new PrintWriter(socket.getOutputStream(), true);
			
			userName = socketReader.readLine();	
			server.broadCast("[" + userName + "]���� �����ϼ̽��ϴ�.");	

			while((message = socketReader.readLine()) != null){
				server.broadCast("[" + userName + "]���� ��: " + message);
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				socketReader.close();		socketWriter.close();
				socket.close();
				System.out.println(socket.getInetAddress() + " ������ ����Ǿ����ϴ�.");
				server.removeThread(this);
				server.broadCast("[" + userName + "]���� �����ϼ̽��ϴ�.");
			} catch (IOException ioe) {	}
		}
	}

	public void sendMessage(String message) {
		socketWriter.println(message);
	}
}

