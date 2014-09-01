package chat2;

import java.net.*;
import java.io.*;

public class ChatServer {
	private ServerSocket serverSocket;

	public ChatServer(int port) {
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
			System.out.println(socket.getInetAddress() + " ���� �����߽��ϴ�.");
			serverThread = new ServerThread(this, socket);
			serverThread.start();

		}
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

	public ServerThread(ChatServer server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}

	public void run() {
		try {
			socketReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			socketWriter = new PrintWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			String message = null;
			while ((message = socketReader.readLine()) != null) {

				System.out.println("Client Message: " + message);
				socketWriter.println("Server Echo : "+message);
				socketWriter.flush();

			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				socketReader.close();
				socketWriter.close();
				socket.close();
				System.out.println(socket.getInetAddress() + " ������ ����Ǿ����ϴ�.");
			} catch (IOException ioe) {
			}
		}
	}

	public void sendMessage(String message) {
		socketWriter.println(message);
	}
}
