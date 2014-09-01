package chat6;

import java.net.*;
import java.io.*;
import java.util.*;

/*  << �������� ������ Protocol ���� >>
 * 100: ���ο� ������ ������ �˸� / ������ ����Ʈ ���� ��û (��: "100|ȫ�浿|����ũ|����")
 * 200: Ư�� ������ ������ �˸� / ������ ����Ʈ ���� ��û (��: "200|ȫ�浿|����")
 * 300: ��ȭ �޽��� ���� / (��: "300|�ȳ��ϼ���!")     */

public class ChatServer {
	private ServerSocket serverSocket;//������Ĺ ���� ����
	private List threadPool;//ServerThread�� �����ϱ� ���� list��ü�� ����
	//constructor����
	public ChatServer(int port) {
		threadPool = new ArrayList(10);//threadPool�� Thread��ü�� 10�� �� �� �ְ� ArrayList��ü�� ����

		try {
			serverSocket = new ServerSocket(port);//ServerSocket��ü ����
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
	//
	public void start() throws IOException {
		Socket socket;//Socket���� ����
		Thread serverThread;//ThreadŸ���� ���� ����
		while (true) {
			socket = serverSocket.accept();//Server��  Client�� ���ӵǱ⸦ ��ٸ�
			System.out.println(socket.getInetAddress() + " ���� �����߽��ϴ�.");
			serverThread = new ServerThread(this, socket);//ServerThreadŸ������ ��ü�� ����
			// addThread(serverThread);
			serverThread.start();//thread�� �����Ŵ
		}
	}

	/*
	 * ������ �����̳� ����� ������ �������� ���� ����Ʈ ���� (��) 100|ȫ�浿|����ũ|���� or 200|ȫ�浿|����
	 */
	public void broadCastUserList(int header) {
		StringBuffer userList = new StringBuffer();//���������� ����� ���� StringBuffer��ü�� ����
		userList.append(header);//header�� �߰� 100: ����� �߰��� 200 : ����� ����
		ServerThread serverThread;//ServerThread���� ����
		//threadPool�� ũ�⸸ŭ �����Ű�� ���� For loop�� �����
		for (int i = 0; i < threadPool.size(); i++) {
			serverThread = (ServerThread) threadPool.get(i);
			userList.append("|"+serverThread.getUserName());
		}
		broadCast(userList.toString());//ä�������� �������� �޽����� ����
	}

	/** ä�� ������ �������� �޽��� ���� */
	public void broadCast(String message) {
		ServerThread serverThread;
		for (int i = 0; i < threadPool.size(); i++) {
			serverThread = (ServerThread) threadPool.get(i);
			serverThread.sendMessage(message);
		}
	}

	/** �ӼӸ�, ������ �� Ư���ο��Ը� ���� */
	public void singleCast(String targetUser, String message) {
		ServerThread serverThread;
		for (int i = 0; i < threadPool.size(); i++) {
			serverThread = (ServerThread) threadPool.get(i);
			if (serverThread.getUserName().equals(targetUser)) {
				//threadPool���θ� Ȯ���ϸ鼭 �� �������� �̸��� 
				//targetUser�� �̸��� ������ �޽����� �����Ҽ� �ֵ��� ����
				serverThread.sendMessage(message);//message�� ����
			}
		}
	}

	/** �յ�, ������ �� Ư������ �����ϰ� ���� */
	public void wangttaCast(String targetUser, String message) {
		ServerThread serverThread;
		for (int i = 0; i < threadPool.size(); i++) {
			serverThread = (ServerThread) threadPool.get(i);
			if (!(serverThread.getUserName().equals(targetUser))) {
				//threadPool���θ� Ȯ���ϸ鼭 �� �������� �̸��� 
				//targetUser�� �̸��� ���� ������ �޽����� �����Ҽ� �ֵ��� ����
				serverThread.sendMessage(message);
			}
		}
	}
	/**threadPool�� thread�� �߰�*/
	public void addThread(Thread aThread) {
		threadPool.add(aThread);
	}
	/**threadPool�� thread�� ����*/
	public void removeThread(Thread aThread) {
		threadPool.remove(aThread);
	}

	public static void main(String[] args) {
		ChatServer server = new ChatServer(5432);
	}
}
/**ServerThread�� ����. Thread�� ��ӹ���*/
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
				System.out.println(socket.getInetAddress() + " ������ ����Ǿ����ϴ�.");
			} catch (IOException ioe) {
			}
		}
	}
/**���������� �м��ϱ� ���� method
	 * 100: ��ȭ ���� / ��ȭ�� ����  (��: "100|ȫ�浿")
	 * 200: ��ȭ ���� / ��ȭ�� ����  (��: "200|�̼���")
	 * 300: ��ü ���� / ä�� ������ �������� �޽��� ���� ��û (��: "300|�ȳ��ϼ���")
	 * 400: �ӼӸ� / ������ �� Ư���ο��Ը� ���� ��û (��: "400|����ũ|�ӼӸ��Դϴ�")
	 * 500: �յ� / ���ڰ� �� Ư������ �����ϰ� ���� ��û (��: "500|�νø�|�νøǽȾ��")
	 * */
	private void parseProtocol(String protocol) {
		StringTokenizer tokenizer = new StringTokenizer(protocol, "|");
		int protocolHeader = Integer.parseInt(tokenizer.nextToken());
		String message = null;
		String targetUser = null;

		switch (protocolHeader) {
		case 100: // �� ������ ����: UserList ����(broadCastUserList() ȣ��)
			userName = tokenizer.nextToken();
			server.addThread(this);
			server.broadCastUserList(100);
			server.broadCast("300|�â�" + userName + "�âô��� �����ϼ̽��ϴ�.");
			break;
		case 200: // ���� ������ ����: UserList ����(broadCastUserList() ȣ��)
			userName = tokenizer.nextToken();
			server.removeThread(this);
			server.broadCastUserList(200);
			server.broadCast("300|�â�" + userName + "�âô��� �����ϼ̽��ϴ�.");
			break;
		case 300: // ä�� ������ �������� �޽��� ����(broadCast() ȣ��)
			message = tokenizer.nextToken();
			server.broadCast("300|[" +userName + "]���� �� :" +message);
			break;
		case 400: // �ӼӸ�: Ư���ο��Ը� ����(singleCast() ȣ��)
			targetUser = tokenizer.nextToken();
			message = tokenizer.nextToken();
			server.singleCast(targetUser,"300|[" +userName + "]���� �� :"+message);
			break;
		case 500: // �յ�: Ư������ �����ϰ� ����(wangttaCast() ȣ��)
			targetUser = tokenizer.nextToken();
			message = tokenizer.nextToken();
			server.wangttaCast(targetUser,"300|[" +userName + "]���� �� :"+message);
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
