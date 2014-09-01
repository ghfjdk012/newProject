package chat7;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

public class ChatClientGUI extends Frame implements ActionListener, ItemListener {
	private Socket socket;
	private ObjectOutputStream socketOOS;
	private ObjectInputStream socketOIS;

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
			socketOOS = new ObjectOutputStream(socket.getOutputStream());
			socketOIS = new ObjectInputStream(socket.getInputStream());

			socketOOS.writeObject(new Data(Data.FIRST_MSG, chatName));

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
				socketOOS.writeObject(new Data(Data.LAST_MSG, chatName));

				System.out.println("���� ������ �����մϴ�.");
				socketOOS.close();		socketOIS.close();
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
		tfHostName = new TextField("localhost", 20);
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
				conDialog.setVisible(false);			}
		});
		btnOK.addActionListener(this);
		btnCancel.addActionListener(this);

		Point point = this.getLocation();  //Point class �� x,y��ǥ�� ����
		conDialog.setLocation(point.x+130, point.y+120);

		conDialog.pack();
		conDialog.setVisible(true);
		tfChatName.requestFocus();
	}

	private void changeBtnState(boolean conn) {
		btnConn.setEnabled(!conn);
		btnDisconn.setEnabled(conn);
		btnSend.setEnabled(conn);
	}

	public void refreshUserList(java.util.List userList) {
		lstUsers.removeAll();
		for (int i = 0; i < userList.size() ; i++) {
			lstUsers.add((String)userList.get(i));
		}
	}

	private final int ALL = 0;
	private final int WHISPER = 1;
	private final int WANGTTA = 2;

	/** ���õ� ��ɿ� ���� protocol �����͸� ���� */
	private Data makeProtocol(String msg) {
		Data protocol = null;
		String selectedUser = lstUsers.getSelectedItem();

		switch (choOption.getSelectedIndex()) {
			case ALL:
				protocol = new Data(Data.NORMAL_MSG, chatName, msg);
				break;
			case WHISPER:
				protocol = new Data(Data.SINGLE_MSG, chatName, selectedUser, msg);
				break;
			case WANGTTA:
				protocol = new Data(Data.WANGTTA_MSG, chatName, selectedUser, msg);
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
			try {
				socketOOS.writeObject(makeProtocol(tfMessage.getText()));
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
			}
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
			try {
				Data protocol = null;
				while (true) {
					protocol = (Data) socketOIS.readObject();
					parseProtocol(protocol);
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}

		private void parseProtocol(Data protocol) {
			int state = protocol.getState();
			String userName = protocol.getUserName();

			switch(state) {
				case Data.FIRST_MSG: // ������ ����
					refreshUserList(protocol.getUserList());	
					taChatArea.append("�â�" + userName + "�âô��� �����ϼ̽��ϴ�.\n");
					break;
				case Data.LAST_MSG: // ������ ����
					refreshUserList(protocol.getUserList());
					taChatArea.append("�â�" + userName + "�âô��� �����ϼ̽��ϴ�.\n");
					break;
				case Data.NORMAL_MSG: // �Ϲ� ��ȭ
					taChatArea.append("[" + userName + "]���� ��: " + protocol.getMessage() + "\n");
					break;
				case Data.SINGLE_MSG: // �ӼӸ�
					if (protocol.getTargetName().equals(chatName)) {
						taChatArea.append("[" + userName + "]���� �ӼӸ�: " + protocol.getMessage() + "\n");
					}
					break;
				case Data.WANGTTA_MSG: // �յ�
					if (!protocol.getTargetName().equals(chatName)) {
						taChatArea.append("[" + userName + "]���� ��: " + protocol.getMessage() + "\n");
					}
					break;
			}
		}
	}
}	
