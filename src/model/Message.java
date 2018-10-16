package model;
import config.Config.MessageType;

public class Message {
	private MessageType type;
	private int length;
	private byte[] payload;

	public Message(int type, int length, byte[] payload) {
		// TODO Auto-generated constructor stub
		MessageType[] messageIndex = MessageType.values();
		this.type = messageIndex[type];
		this.length = length;
		this.payload = payload;
	}
	public MessageType getType() {
		return type;
	}
	public int getLength() {
		return length;
	}
	public byte[] length() {
		return payload;
		
	}

}
