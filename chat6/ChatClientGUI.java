package chat6;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.StringTokenizer;
/*  << Ŭ���̾�Ʈ���� ������ Protocol ���� >>
 * 100: ��ȭ ���� / ��ȭ�� ����  (��: "100|ȫ�浿")
 * 200: ��ȭ ���� / ��ȭ�� ����  (��: "200|�̼���")
 * 300: ��ü ���� / ä�� ������ �������� �޽��� ���� ��û (��: "300|�ȳ��ϼ���")
 * 400: �ӼӸ� / ������ �� Ư���ο��Ը� ���� ��û (��: "400|����ũ|�ӼӸ��Դϴ�")
 * 500: �յ� / ���ڰ� �� Ư������ �����ϰ� ���� ��û (��: "500|�νø�|�νøǽȾ��") */

public class ChatClientGUI extends Frame implements ActionListener, ItemListener {
	private Socket socket;
	private PrintWriter socketWriter;
	private BufferedReader socketReader;

	private TextArea taChatArea;
	private TextField tfMessage;
	private Button btnSend, btnConn, btnDisconn;
	private List lstUsers;
	private Choice choOption;

	// For Dialog
	private Dialog conDialog;
	private TextField tfHostName, tfChatName;
	private Button btnOK, btnCancel;

	private String hostName;
	private int port;
	private String chatName;


	public ChatClientGUI(String host, int port) {
		super("���� ä�ù�"); 
		this.hostName = host;
		this.port = port;
		launchFrame();
	}

	private void connect(String host, int port) {
		try {
			socket = new Socket(host, port);
			socketWriter = new PrintWriter(socket.getOutputStream(), true);
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			socketWriter.println("100|" + chatName);
			//socketWriter.println(chatName);

			new ClientThread().start();
			changeBtnState(true);		

			System.out.println("������ �����Ͽ����ϴ�.(" + host + ":" + port + ")");
		} catch (IOException e) {
			System.out.println("���� ���� ���� : " + e.getMessage());
			System.exit(-1);
		}
	}
	
	private void disconnect() {
		if(socket != null) {
			try {
				socketWriter.println("200|" + chatName);
				
				System.out.println("���� ������ �����մϴ�.");
				socketWriter.close();	socketReader.close();
				socket.close();
				changeBtnState(false);	
			} catch(IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		}
	}

	private void launchFrame() {
		taChatArea = new TextArea("", 10, 40);
		tfMessage = new TextField("", 40);
		btnSend = new Button("������");
		btnConn = new Button("��������");
		btnDisconn = new Button("��������");
		lstUsers = new List(10, false);
		choOption = new Choice();
		choOption.add("����ü");
		choOption.add("�ӼӸ�");
		choOption.add("�յ�");

		Panel pSouth = new Panel();
		pSouth.add(choOption);
		pSouth.add(tfMessage);
		pSouth.add(btnSend);
		pSouth.setBackground(Color.darkGray);

		Panel pNorth = new Panel();
		pNorth.add(btnConn);
		pNorth.add(btnDisconn);
		pNorth.setBackground(Color.orange);

		Panel pCenter = new Panel(new BorderLayout());
		Panel pCenterCenter = new Panel(new BorderLayout());
		Panel pCenterEast = new Panel(new BorderLayout());
		pCenterCenter.add(new Label("   ��ȭ ����"), BorderLayout.NORTH);
		pCenterCenter.add(taChatArea, BorderLayout.CENTER);
		pCenterEast.add(new Label("������ ����Ʈ", Label.CENTER), BorderLayout.NORTH);
		pCenterEast.add(lstUsers, BorderLayout.CENTER);
		pCenter.add(pCenterCenter, BorderLayout.CENTER);
		pCenter.add(pCenterEast, BorderLayout.EAST);

		add(pSouth, BorderLayout.SOUTH);
		add(pCenter, BorderLayout.CENTER);
		add(pNorth, BorderLayout.NORTH);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				disconnect();
				System.exit(0);
			}
		});

		tfMessage.addActionListener(this);
		btnSend.addActionListener(this);
		btnConn.addActionListener(this);
		btnDisconn.addActionListener(this);
		lstUsers.addItemListener(this);
		choOption.addItemListener(this);

		setSize(500, 350);
	    Dimension dm = this.getToolkit().getScreenSize();
		int x = dm.width/2 - this.getWidth()/2;
		int y = dm.height/2 - this.getHeight()/2;
	    setLocation(x, y);
		setResizable(false);
		setVisible(true);

		taChatArea.setEditable(false);
		tfMessage.requestFocus();
		changeBtnState(false);

		launchDialog();
	}

	public void launchDialog() {
		conDialog = new Dialog(this, "���ἳ��", true);

		Panel pLabel = new Panel(new GridLayout(2, 1));     
		Panel pTextField = new Panel(new GridLayout(2, 1));
		Panel pCenter = new Panel();
		Panel pSouth = new Panel();

		Label lbHostName = new Label("ȣ��Ʈ��", Label.LEFT);
		tfHostName = new TextField("localhost");
		Label lbChatName = new Label("��ȭ��", Label.LEFT);
		tfChatName = new TextField(20);
				
		btnOK = new Button("Ȯ��");
		btnCancel = new Button("���");
		
		pLabel.add(lbHostName);
		pLabel.add(lbChatName);
		pTextField.add(tfHostName);
		pTextField.add(tfChatName);
		pCenter.add(pLabel);
		pCenter.add(pTextField);
		pSouth.add(btnOK);
		pSouth.add(btnCancel);
		conDialog.add(pCenter, BorderLayout.CENTER);
		conDialog.add(pSouth, BorderLayout.SOUTH);	

		conDialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				conDialog.dispose();
			}
		});
		btnOK.addActionListener(this);
		btnCancel.addActionListener(this);

		Point point = this.getLocation();  //Point class �� x,y��ǥ�� ����
		conDialog.setLocation(point.x+130, point.y+120);

		conDialog.pack();
		conDialog.setVisible(true);
		tfHostName.requestFocus();
	}

	private void changeBtnState(boolean conn) {
		btnConn.setEnabled(!conn);
		btnDisconn.setEnabled(conn);
		btnSend.setEnabled(conn);
	}


	private final int ALL = 0;
	private final int WHISPER = 1;
	private final int WANGTTA = 2;

	/** ���õ� ��ɿ� ���� protocol �޽����� ���� */
	private String makeProtocol(String msg) {
		String protocol = null;
		String selectedUser = lstUsers.getSelectedItem();

		switch (choOption.getSelectedIndex()) {
			case ALL:
				protocol = "300|" + msg;
				break;
			case WHISPER:
				protocol = "400|" + selectedUser + "|" + msg;
				break;
			case WANGTTA:
				protocol = "500|" + selectedUser + "|" + msg;
				break;
		}
		return protocol;
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source==btnConn) {
			tfHostName.setText(hostName);
			tfChatName.setText(chatName);
			Point point = this.getLocation();  //Point class �� x,y��ǥ�� ����
			conDialog.setLocation(point.x+150, point.y+150);
			conDialog.setVisible(true);
		} else if (source==btnDisconn) {
			disconnect();
			lstUsers.removeAll();
			taChatArea.append("�������� ������ �����Ͽ����ϴ�.\n");
		} else if (source==btnOK) {
			this.hostName = tfHostName.getText().trim();
			this.chatName = tfChatName.getText().trim();
			conDialog.setVisible(false);
			connect(hostName, port);
		} else if (source==btnCancel) {
			conDialog.setVisible(false);
		} else if (source==tfMessage || source == btnSend) {
			// makeProtocol() �޼ҵ带 ����Ͽ� ���� �������� �޽����� ����
			socketWriter.println(makeProtocol(tfMessage.getText()));
			tfMessage.setText("");
			tfMessage.requestFocus();
		}
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if((choOption.getSelectedIndex()!=ALL) && (lstUsers.getSelectedIndex()==-1)) { 
			taChatArea.append("== ���� UserList���� ����ڸ� ������ �ּ���! ==\n");
			choOption.select(ALL);
		}
	}

	public static void main(String[] args) {
		ChatClientGUI client = new ChatClientGUI("localhost", 5432);
	}

	class ClientThread extends Thread {
		public void run() {
			String broadCastMessage = null;
			StringTokenizer tokenizer = null;
			String protocolHeader = null;

			try {
				while ((broadCastMessage = socketReader.readLine()) != null) {
					tokenizer = new StringTokenizer(broadCastMessage, "|");
					protocolHeader = tokenizer.nextToken();
					if (protocolHeader.equals("100")|| protocolHeader.equals("200")) {
						lstUsers.removeAll();
						while(tokenizer.hasMoreTokens()) {
							lstUsers.add(tokenizer.nextToken());
						}
					} else if (protocolHeader.equals("300")) {
						taChatArea.append(tokenizer.nextToken() + "\n");
					}
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
}	
