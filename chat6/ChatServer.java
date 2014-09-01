package chat6;

import java.net.*;
import java.io.*;
import java.util.*;

/*  << 서버에서 전송할 Protocol 정의 >>
 * 100: 새로운 참가자 입장을 알림 / 참가자 리스트 갱신 요청 (예: "100|홍길동|히딩크|쿠엘류")
 * 200: 특정 참가자 퇴장을 알림 / 참가자 리스트 갱신 요청 (예: "200|홍길동|쿠엘류")
 * 300: 대화 메시지 전송 / (예: "300|안녕하세요!")     */

public class ChatServer {
	private ServerSocket serverSocket;//서버소캣 변수 선언
	private List threadPool;//ServerThread를 관리하기 위한 list객체를 선언
	//constructor선언
	public ChatServer(int port) {
		threadPool = new ArrayList(10);//threadPool를 Thread객체가 10개 들어갈 수 있게 ArrayList객체로 생성

		try {
			serverSocket = new ServerSocket(port);//ServerSocket객체 생성
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
	//
	public void start() throws IOException {
		Socket socket;//Socket변수 선언
		Thread serverThread;//Thread타입의 변수 선언
		while (true) {
			socket = serverSocket.accept();//Server와  Client가 접속되기를 기다림
			System.out.println(socket.getInetAddress() + " 에서 접속했습니다.");
			serverThread = new ServerThread(this, socket);//ServerThread타입으로 객체를 생성
			// addThread(serverThread);
			serverThread.start();//thread를 실행시킴
		}
	}

	/*
	 * 참가자 입장이나 퇴장시 참가자 전원에게 접속 리스트 전송 (예) 100|홍길동|히딩크|쿠엘류 or 200|홍길동|쿠엘류
	 */
	public void broadCastUserList(int header) {
		StringBuffer userList = new StringBuffer();//프로토콜을 만들기 위한 StringBuffer객체를 생성
		userList.append(header);//header를 추가 100: 사람이 추가됨 200 : 사람이 나감
		ServerThread serverThread;//ServerThread변수 설정
		//threadPool의 크기만큼 실행시키기 위해 For loop를 사용함
		for (int i = 0; i < threadPool.size(); i++) {
			serverThread = (ServerThread) threadPool.get(i);
			userList.append("|"+serverThread.getUserName());
		}
		broadCast(userList.toString());//채팅참가자 전원에게 메시지를 전송
	}

	/** 채팅 참가자 전원에게 메시지 전송 */
	public void broadCast(String message) {
		ServerThread serverThread;
		for (int i = 0; i < threadPool.size(); i++) {
			serverThread = (ServerThread) threadPool.get(i);
			serverThread.sendMessage(message);
		}
	}

	/** 귓속말, 참가자 중 특정인에게만 전송 */
	public void singleCast(String targetUser, String message) {
		ServerThread serverThread;
		for (int i = 0; i < threadPool.size(); i++) {
			serverThread = (ServerThread) threadPool.get(i);
			if (serverThread.getUserName().equals(targetUser)) {
				//threadPool전부를 확인하면서 그 접속자의 이름이 
				//targetUser의 이름과 같으면 메시지를 전송할수 있도록 만듬
				serverThread.sendMessage(message);//message를 전송
			}
		}
	}

	/** 왕따, 참가자 중 특정인을 제외하고 전송 */
	public void wangttaCast(String targetUser, String message) {
		ServerThread serverThread;
		for (int i = 0; i < threadPool.size(); i++) {
			serverThread = (ServerThread) threadPool.get(i);
			if (!(serverThread.getUserName().equals(targetUser))) {
				//threadPool전부를 확인하면서 그 접속자의 이름이 
				//targetUser의 이름과 같지 않으면 메시지를 전송할수 있도록 만듬
				serverThread.sendMessage(message);
			}
		}
	}
	/**threadPool에 thread를 추가*/
	public void addThread(Thread aThread) {
		threadPool.add(aThread);
	}
	/**threadPool에 thread를 제거*/
	public void removeThread(Thread aThread) {
		threadPool.remove(aThread);
	}

	public static void main(String[] args) {
		ChatServer server = new ChatServer(5432);
	}
}
/**ServerThread를 선언. Thread를 상속받음*/
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

			while ((message = socketReader.readLine()) != null) {
				parseProtocol(message);
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				socketReader.close();
				socketWriter.close();
				socket.close();
				System.out.println(socket.getInetAddress() + " 접속이 종료되었습니다.");
			} catch (IOException ioe) {
			}
		}
	}
/**프로토콜을 분석하기 위한 method
	 * 100: 대화 참여 / 대화명 전송  (예: "100|홍길동")
	 * 200: 대화 종료 / 대화명 전송  (예: "200|이순신")
	 * 300: 전체 전송 / 채팅 참가자 전원에게 메시지 전송 요청 (예: "300|안녕하세요")
	 * 400: 귓속말 / 참가자 중 특정인에게만 전송 요청 (예: "400|히딩크|귓속말입니다")
	 * 500: 왕따 / 참자가 중 특정인을 제외하고 전송 요청 (예: "500|부시맨|부시맨싫어요")
	 * */
	private void parseProtocol(String protocol) {
		StringTokenizer tokenizer = new StringTokenizer(protocol, "|");
		int protocolHeader = Integer.parseInt(tokenizer.nextToken());
		String message = null;
		String targetUser = null;

		switch (protocolHeader) {
		case 100: // 새 참가자 입장: UserList 갱신(broadCastUserList() 호출)
			userName = tokenizer.nextToken();
			server.addThread(this);
			server.broadCastUserList(100);
			server.broadCast("300|▣▣" + userName + "▣▣님이 입장하셨습니다.");
			break;
		case 200: // 기존 참가자 퇴장: UserList 갱신(broadCastUserList() 호출)
			userName = tokenizer.nextToken();
			server.removeThread(this);
			server.broadCastUserList(200);
			server.broadCast("300|▣▣" + userName + "▣▣님이 퇴장하셨습니다.");
			break;
		case 300: // 채팅 참가자 전원에게 메시지 전송(broadCast() 호출)
			message = tokenizer.nextToken();
			server.broadCast("300|[" +userName + "]님의 말 :" +message);
			break;
		case 400: // 귓속말: 특정인에게만 전송(singleCast() 호출)
			targetUser = tokenizer.nextToken();
			message = tokenizer.nextToken();
			server.singleCast(targetUser,"300|[" +userName + "]님의 말 :"+message);
			break;
		case 500: // 왕따: 특정인을 제외하고 전송(wangttaCast() 호출)
			targetUser = tokenizer.nextToken();
			message = tokenizer.nextToken();
			server.wangttaCast(targetUser,"300|[" +userName + "]님의 말 :"+message);
			break;
		}
	}

	public void sendMessage(String message) {
		socketWriter.println(message);
	}

	public String getUserName() {
		return userName;
	}
}
