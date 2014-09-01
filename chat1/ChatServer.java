package chat1;

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

		while (true) {
			socket = serverSocket.accept();
			System.out.println(socket.getInetAddress() + " ���� �����߽��ϴ�.");
			service(socket);
		}
	}

	private BufferedReader socketReader;
	private PrintWriter socketWriter;

	private void service(Socket socket) {
		String message = null;
		try {
			socketReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			socketWriter = new PrintWriter(new OutputStreamWriter(
					socket.getOutputStream()));

			while ((message = socketReader.readLine()) != null) {

				System.out.println("Client Message: " + message);

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

	public static void main(String[] args) {
		ChatServer server = new ChatServer(5432);
	}
}