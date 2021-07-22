package com.smart.helper;

public class Message {

	private String content;
	private String type;
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public Message(String string, String string2) {
		super();
		// TODO Auto-generated constructor stub
	}
	public Message(String string) {
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "Message [content=" + content + ", type=" + type + "]";
	}
	
}
