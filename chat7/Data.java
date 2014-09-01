package chat7;

import java.io.*;
import java.util.*;
/*  Server�� Client ���̿��� �ְ� ���� ��ü�� ä�� ������ ����ȴ�. */
public class Data implements Serializable {

	private int state;					// Client�� ���¸� ��Ÿ��  ex) ó�� ����, ���� ���� ��
	private String userName;		// �޽����� ������ Client�� �̸�
	private String targetName;	// �ӼӸ��̳� �յ��� ��� �̸�
	private String message;		// Client�� ������ �޽���
	private List userList;			// ���� ������ ����Ʈ�� ����

	/* Client�� ���¸� �����ϴ� ���  */
	public static final int FIRST_MSG = 100;		// ó�� ����
	public static final int LAST_MSG = 200;		// ���� ����
	public static final int NORMAL_MSG = 300;	// ���� ��ȭ
	public static final int SINGLE_MSG = 400;	// �ӼӸ�
	public static final int WANGTTA_MSG = 500;	// �յ�

	/* FIRST_MSG, LAST_MSG �� ������	 */
	public Data (int state, String userName) {		
		this.state = state;
		this.userName = userName;
	}
	/* NORMAL_MSG �� ������ */
	public Data (int state, String userName, String message) {	
		this(state, userName);
		this.message = message;
	}
	/* SINGLE_MSG, WANGTTA_MSG �� ������ */
	public Data (int state, String userName, String targetName, String message) {
		this(state, userName, message);
		this.targetName = targetName;
	}

	public int getState() {
		return state;
	}

	public String getUserName() {
		return userName;
	}

	public String getTargetName() {
		return targetName;
	}

	public String getMessage() {
		return message;
	}

	public List getUserList() {
		return userList;
	}

	public void setUserList(List userList) {
		this.userList = userList;
	}

	public String toString() {
		return "Data: " + state + "|" + userName + "|" 
				+ targetName + "|" + message + "|" + userList; 
	}
}