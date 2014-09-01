package chat4;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.nio.ByteOrder;
import java.io.*;

public class ChatClientGUI extends Frame implements ActionListener {
	private Socket socket;
	private PrintWriter socketWriter;
	private BufferedReader socketReader;

	private TextArea taChatArea;
	private TextField tfMessage;
	private Button btnSend;

	public ChatClientGUI(String host, int port) {
		super("허접 채팅방");
		connect(host, port);
		launchFrame();
	}

	private void connect(String host, int port) {
		try {
			socket = new Socket(host, port);
			socketWriter = new PrintWriter(socket.getOutputStream(), true);
			socketReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			new ClientThread().start();
			System.out.println("서버에 접속하였습니다.(" + host + ":" + port + ")");
		} catch (IOException e) {
			System.out.println("서버 접속 실패 : " + e.getMessage());
			System.exit(-1);
		}
	}

	private void disconnect() {
		if (socket != null) {
			try {
				System.out.println("서버 접속을 종료합니다.");
				socketWriter.close();
				socketReader.close();
				socket.close();
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		}
	}

	private void launchFrame() {
		taChatArea = new TextArea("[먼저 이름을 입력하세요]\n");
		tfMessage = new TextField(40);
		btnSend = new Button("보내기");
		Panel pan = new Panel();
		add(taChatArea, BorderLayout.CENTER);
		add(pan, BorderLayout.SOUTH);
		pan.setBackground(Color.WHITE);
		pan.add(tfMessage);
		pan.add(btnSend);
		taChatArea.setEditable(false);
		setVisible(true);
		setResizable(false);
		setBounds(0, 0, 400, 300);
		btnSend.addActionListener(this);
		tfMessage.addActionListener(this);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				System.exit(0);
			}

		});

	}

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj.equals(btnSend)) {
			socketWriter.println(tfMessage.getText());
			tfMessage.setText("");
		} else if (obj.equals(tfMessage)) {
			socketWriter.println(tfMessage.getText());
			tfMessage.setText("");
		}

	}

	public static void main(String[] args) {
		ChatClientGUI client = new ChatClientGUI("localhost", 5432);
	}

	class ClientThread extends Thread {
		public void run() {
			String broadCastMessage = null;
			try {
				while ((broadCastMessage = socketReader.readLine()) != null) {
					taChatArea.append(broadCastMessage + "\n");
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
}