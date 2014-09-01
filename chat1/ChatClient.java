package chat1;

import java.net.*;
import java.awt.peer.SystemTrayPeer;
import java.io.*;

public class ChatClient {
	private Socket socket;

	public ChatClient(String host, int port) {
		System.out.println("::::: 허접 채팅 클라이언트입니다. :::::");
		connect(host, port);
		start();
	}

	public void connect(String host, int port) {
		try {
			socket = new Socket(host, port);
			System.out.println("서버에 접속하였습니다.(" + host + ":" + port + ")");
		} catch (IOException e) {
			System.out.println("서버 접속 실패: " + e.getMessage());
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
			socketWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			System.out.println("메시지를 입력하세요. (입력종료: Ctrl+Z)");
			while ((message = keyReader.readLine()) !=null) {
				System.out.println(message);
				socketWriter.println(message);
				socketWriter.flush();
			}

			System.out.println("서버 접속을 종료합니다.");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				socketWriter.close();
				socketReader.close();
				socket.close();
			} catch (IOException ioe) {
			}
		}
	}

	public static void main(String[] args) {
		ChatClient client = new ChatClient("localhost", 5432);
	}
}
