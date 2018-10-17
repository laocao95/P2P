package model;
import custom.Util;
import custom.Config.*;

public class HandShakeMessage extends Message{
	private String header;
	private int peerID;
	public HandShakeMessage(String header, int peerID) {
		super(MessageType.HANDSHAKE, null);
		this.header = header;
		this.peerID = peerID;
	}
	public String getHeader() {
		return header;
	}
	public int getPeerID() {
		return peerID;
	}
	
	@Override
	public byte[] toBytes() {
		byte[] message = new byte[32];
		byte[] headerBytes = header.getBytes();
		byte[] peerIDBytes = Util.IntToByte(peerID);
		System.arraycopy(headerBytes, 0, message, 0, headerBytes.length);
		System.arraycopy(peerIDBytes, 0, message, 28, peerIDBytes.length);
		return message;
	}
	//inherit getType function
}
