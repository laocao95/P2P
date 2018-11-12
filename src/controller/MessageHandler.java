package controller;

import model.*;
import custom.*;
import custom.Config.MessageType;
import java.math.*;

public class MessageHandler {
	private static class SingletonHolder {
		public final static MessageHandler instance = new MessageHandler();
	}
	private MessageHandler() {
		// TODO Auto-generated constructor stub
	}
	
	public static MessageHandler getInstance() {
		return SingletonHolder.instance;
	}

	public void handleHandshakeMessage(Connection connect, Message message) throws Exception{
		//check header
		String header = "P2PFILESHARINGPROJ";

		HandShakeMessage handShakeMessage = (HandShakeMessage)message;
		
		if (!handShakeMessage.getHeader().equals(header)) {
			throw new Exception("error handshake header");
		}
		
		if (connect.getPeerInfo() == null) {
			int peerId = handShakeMessage.getPeerID();
			PeerInfo peerInfo = PeerInfoManager.getInstance().getPeerInfoById(peerId);
			connect.setPeerInfo(peerInfo);
			connect.setReceivedHandShake();
			sendHandShakeMessage(connect);
		} else {
			//means I am the handshake initiator, no need to send handshake again
			connect.setReceivedHandShake();
			
		}	
		System.out.println("receive handshake from " + connect.getPeerInfo().getId());
		
	}
	
	public void sendHandShakeMessage(Connection connect) throws Exception{
		
		String header = "P2PFILESHARINGPROJ";
		
		HandShakeMessage message = new HandShakeMessage(header, PeerInfoManager.getInstance().getMyInfo().getId());
		
		connect.sendMessage(message);
		
		System.out.println("send handshake message to " + connect.getPeerInfo().getId());
	}
	
	public void handleBitFieldMessage(Connection connect, Message message) throws Exception{
		Message bitField = (Message)message;
		PeerInfo peerInfo = connect.getPeerInfo();
		byte[] payLoad = bitField.getPayload();
		int pieceNum = BitfieldManager.getInstance().getpieceNum();
		int byteNum = (int)Math.ceil((double)pieceNum/8);
		for (int i = 0; i < byteNum; i++){
			if (i != byteNum - 1){
				for (int j = 0; j < 8; j++){
					//update bitfield if the correspond bit is 1
					int field = (payLoad[i] >> 7-j) & 1;
					if (field == 1){
						BitfieldManager.getInstance().updateBitfield(peerInfo, j + i * 8);
					}
				}
			}
			//the last byte may contain extra 0s
			else {
				int remainBit = pieceNum - i * 8;
				for (int j = 0; j < remainBit; j++){
					//update bitfield if the correspond bit is 1
					int field = (payLoad[i] >> 7-j) & 1;
					if (field == 1){
						BitfieldManager.getInstance().updateBitfield(peerInfo, j + i * 8);
					}
				}
			}
		}
	}
	
	public void handleHaveMessage(Connection connect, Message message) throws Exception{
		Message have = (Message)message;
		byte[] payLoad = have.getPayload();
		int pieceNum = Util.Byte2Int(payLoad);
		PeerInfo peerInfo = connect.getPeerInfo();
		BitfieldManager.getInstance().updateBitfield(peerInfo, pieceNum);
	}
	
	public void handleUnchokedMessage(Connection connect, Message message) throws Exception{
		int resultOfCAC;
		resultOfCAC = BitfieldManager.getInstance().compareAndchoose(connect.getPeerInfo());
		if(resultOfCAC == -1) {
			System.out.println("No interesting block");
		}
		else {
			byte[] payload = Util.IntToByte(resultOfCAC);
			Message request = new Message(MessageType.REQUEST, payload);
			connect.sendMessage(request);
		}
	}
}
