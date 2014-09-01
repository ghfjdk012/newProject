package chat3;

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {
	private ServerSocket serverSocket;
	private List<Thread> threadPool;

	public ChatServer(int port) {
		threadPool = new ArrayList<Thread>();

		try {
			serverSocket = new ServerSocket(port);
			System.out.println(port + " 포트에서 접속을 기다립니다.");
			start();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println("채팅 서버가 종료됩니다.");
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException ioe) {
				}
			}
		}
	}

	public void start() throws IOException {
		Socket socket;
		Thread serverThread;
		while (true) {
			socket = serverSocket.accept();
			System.out.println(socket.getInetAddress() + " 에서 접속했습니다.");
			serverThread = new ServerThread(this, socket);
			addThread(serverThread);
			serverThread.start();
		}
	}

	public void broadCast(String message) {
		ServerThread serverThread;
		for (Thread st : threadPool) {
			serverThread = (ServerThread) st;
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
			socketReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			socketWriter = new PrintWriter(socket.getOutputStream(), true);

			userName = socketReader.readLine();
			 server.broadCast("[" + userName + "]님이 입장하셨습니다.");

			while ((message = socketReader.readLine()) != null) {
				System.out.println("Client Message : " + message);
				server.broadCast("[" + userName + "]님의 말: " + message);
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				socketReader.close();
				socketWriter.close();
				socket.close();
				System.out.println(socket.getInetAddress() + " 접속이 종료되었습니다.");

				 server.broadCast("[" + userName + "]님이 퇴장하셨습니다.");
			} catch (IOException ioe) {
			}
		}
	}

	public void sendMessage(String message) {
		socketWriter.println(message);
	}
}
