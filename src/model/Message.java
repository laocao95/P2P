package model;
import custom.Util;
import custom.Config.MessageType;
public class Message {
	private MessageType type;
	private byte[] payload;

	public Message(MessageType type, byte[] payload) {
		// TODO Auto-generated constructor stub
		this.type = type;
		this.payload = payload;
	}
	public MessageType getType() {
		return type;
	}
	public byte[] getPayload() {
		return payload;
		
	}
	public byte[] toBytes() {
		//need to check valid
		int size = 4 + 1 + payload.length;
		byte[] message = new byte[size];
		
		byte messageType = (byte)String.valueOf(type.ordinal()).charAt(0);
		byte[] messageLength = Util.IntToByte(payload.length + 1);
		
		System.arraycopy(messageLength, 0, message, 0, 3);
		message[4] = messageType;
		System.arraycopy(payload, 0, message, 5, payload.length);
		return message;
	}

}
