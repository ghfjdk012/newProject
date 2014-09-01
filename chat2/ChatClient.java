package chat2;

import java.net.*;
import java.io.*;

public class ChatClient {
	private Socket socket;

	public ChatClient(String host, int port) {
		System.out.println("::::: ���� ä�� Ŭ���̾�Ʈ :::::");
		connect(host, port);
		start();		
	}

	public void connect(String host, int port) {
		try {
			socket = new Socket(host, port);
			System.out.println("������ �����Ͽ����ϴ�.(" + host + ":" + port + ")");
		} catch (IOException e) {
			System.out.println("���� ���� ����: " + e.getMessage());
			System.exit(-1);
		}
	}

	private BufferedReader keyReader;
	private PrintWriter socketWriter;
	private BufferedReader socketReader;

	public void start() {
		String message = null;
		try {
			keyReader = new BufferedReader(new InputStreamReader(System.in));
			socketWriter = new PrintWriter(socket.getOutputStream(), true);
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			System.out.println("�޽����� �Է��ϼ���. (�Է�����: Ctrl+Z)");
			while((message = keyReader.readLine()) != null) {
				socketWriter.println(message);
				message = socketReader.readLine();
				System.out.println(message);
			}

			System.out.println("���� ������ �����մϴ�.");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				socketWriter.close();	socketReader.close();
				socket.close();
			} catch (IOException ioe) {	}
		}
	}

	public static void main(String[] args) {
		ChatClient client = new ChatClient("localhost", 5432);
	}
}
