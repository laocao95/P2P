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
			System.out.println("receive handshake from " + connect.getPeerInfo().getId());
			//send handshake
			sendHandShakeMessage(connect);
			//send bitfield
			sendBitfieldMessage(connect);
		} else {
			//I am the handshake initiator, no need to send handshake again
			connect.setReceivedHandShake();
			System.out.println("receive handshake from " + connect.getPeerInfo().getId());
		}	
	}
	
	public void sendHandShakeMessage(Connection connect) throws Exception{
		
		String header = "P2PFILESHARINGPROJ";
		
		HandShakeMessage message = new HandShakeMessage(header, PeerInfoManager.getInstance().getMyInfo().getId());
		
		connect.sendMessage(message);
		
		System.out.println("send handshake message to " + connect.getPeerInfo().getId());
	}
	public void sendBitfieldMessage(Connection connect) throws Exception{
		boolean[] myBitfield = BitfieldManager.getInstance().getBitField(PeerInfoManager.getInstance().getMyInfo());
		int byteNum = (int) Math.ceil((double)myBitfield.length / 8);
		byte[] payload = new byte[byteNum];
		for (int i = 0; i < byteNum; i++) {
			if (i == byteNum - 1) {
				int offset = 7;
				for (int t = i * 8; t < myBitfield.length; t++) {
					payload[i] += myBitfield[t]?1<<offset:0;
					offset--;
				}
			} else {
				int offset = 7;
				for (int t = i * 8; t < i * 8 + 8; t++) {
					payload[i] += myBitfield[t]?1<<offset:0;
					offset--;
				}
			}
		}
		Message bitfieldMessage = new Message(MessageType.BITFIELD, payload);
		connect.sendMessage(bitfieldMessage);
		System.out.println("sendBitfiedMessage");
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
		
		boolean[] bitfield = BitfieldManager.getInstance().getBitField(peerInfo);
		System.out.println("receive bitfield length = " + bitfield.length);
		String out = "";
		for (int i = 0; i < bitfield.length; i++) {
			if (bitfield[i] == true) {
				out += "1";
			} else if (bitfield[i] == false) {
				out += "0";
			}
		}
		System.out.println("receive bitfield" + out);
		
		if (BitfieldManager.getInstance().comparePeerInfo(peerInfo)){
			Message interested = new Message(MessageType.INTERESTED, null);
			connect.sendMessage(interested);
		}
		else{
			Message notInterested = new Message(MessageType.NOT_INTERESTED, null);
			connect.sendMessage(notInterested);
		}
		
	}
	
	public void handleHaveMessage(Connection connect, Message message) throws Exception{
		Message have = (Message)message;
		byte[] payLoad = have.getPayload();
		int pieceNum = Util.Byte2Int(payLoad);
		PeerInfo peerInfo = connect.getPeerInfo();
		BitfieldManager.getInstance().updateBitfield(peerInfo, pieceNum);
		if (BitfieldManager.getInstance().comparePeerInfo(peerInfo)){
			Message interested = new Message(MessageType.INTERESTED, null);
			connect.sendMessage(interested);
		}
		else{
			Message notInterested = new Message(MessageType.NOT_INTERESTED, null);
			connect.sendMessage(notInterested);
		}
	}
	
	public void handleUnchokedMessage(Connection connect, Message message) throws Exception{
		int resultOfCAC;
		resultOfCAC = BitfieldManager.getInstance().compareAndchoose(connect.getPeerInfo());
		if(resultOfCAC == -1) {
			Message notInterested = new Message(MessageType.NOT_INTERESTED, null);
			connect.sendMessage(notInterested);
		}
		else {
			byte[] payload = Util.IntToByte(resultOfCAC);
			Message request = new Message(MessageType.REQUEST, payload);
			connect.sendMessage(request);
		}
	}
	
	public void handleInterestedMessage(Connection connect) throws Exception{
		//Message interest = (Message)message;
		connect.setInterested();
	}
	
	public void handleUninterestedMessage(Connection connect) throws Exception{
		//Message notInterest = (Message)message;
		connect.setNotInterested();
	}
	
	public void handleRequestMessage(Connection connect, Message message) throws Exception{
		int pieceNumber;
		byte[] pieceIndex = new byte[4];
		System.arraycopy(message.getPayload(), 0, pieceIndex, 0, 4);
		pieceNumber = Util.Byte2Int(pieceIndex);
		byte[] pieceContent = FileManager.getInstance().read(pieceNumber);
		byte[] payload = new byte[4 + pieceContent.length];
		System.arraycopy(pieceIndex, 0, payload, 0, 4);
		System.arraycopy(pieceContent, 0, payload, 4, pieceContent.length);			//link Index and Content
		Message piece = new Message(MessageType.PIECE, payload);
		connect.sendMessage(piece);
	}
	
	public void handlePieceMessage(Connection connect, Message message) throws Exception{
		Message piece = (Message)message;
		byte[] payLoad = piece.getPayload();
		byte[] pieceIndex = new byte[4];
		byte[] pieceContent = new byte[payLoad.length - 4];
		System.arraycopy(payLoad, 0, pieceIndex, 0, 4);
		System.arraycopy(payLoad, 4, pieceContent, 0, pieceContent.length);
		int pieceNum = Util.Byte2Int(pieceIndex);
		FileManager.getInstance().write(pieceNum, pieceContent);
		PeerInfo peerInfo = connect.getPeerInfo();
		BitfieldManager.getInstance().updateBitfield(peerInfo, pieceNum);			//update Bitfield
		int resultOfCAC;
		resultOfCAC = BitfieldManager.getInstance().compareAndchoose(connect.getPeerInfo());
		
		byte[] payload = Util.IntToByte(resultOfCAC);
		//broadcast have message
		//need a loop here!!!!!!!!
		Message have = new Message(MessageType.HAVE, payload);					//send have Piece number
		connect.sendMessage(have);
		if (connect.getpeerChokeMe() == false){
			if(resultOfCAC == -1) {
				Message notInterested = new Message(MessageType.NOT_INTERESTED, null);	//Not interested
				connect.sendMessage(notInterested);
			}
			else {
				Message request = new Message(MessageType.REQUEST, payload);			//request random one
				connect.sendMessage(request);
			}
		}
	}
}
