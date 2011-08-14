package eu.grigis.gaetan.rc;

import java.io.Serializable;
import java.util.HashMap;


public class DataTransfer implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private HashMap<String,String> data;
	private String mail;
	private String sender;
	private String id;
	private String type;
	
	public DataTransfer()
	{
		setMail("");
		setId("");
		setType("");
		setSender("");
	}
	
	public DataTransfer(String m,String s,String i,String t) {
		setMail(m);
		setId(i);
		setType(t);
		setSender(s);
	}
	
	public void setData(HashMap<String,String> data) {
		this.data = data;
	}

	public HashMap<String,String> getData() {
		return data;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getMail() {
		return mail;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getSender() {
		return sender;
	}
}
