package controller;

import model.*;
import custom.*;
import custom.Config.MessageType;

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
		int pieceSize = BitfieldManager.getInstance().getpieceNum();
		for (int i = 0; i < pieceSize;i++){
			for (int j = 0; j < 8; j++){
				//update bitfield if the correspond bit is 1
				int field = (payLoad[i] >> 7-j) & 1;
				if (field == 1){
					BitfieldManager.getInstance().updateBitfield(peerInfo, j + i * 8);
				}
				
			}
		}
		System.out.println(BitfieldManager.getInstance().getBitField(peerInfo));
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
			Message notInterested = new Message(MessageType.NOT_INTERESTED, null);
			connect.sendMessage(notInterested);
		}
		else {
			byte[] payload = Util.IntToByte(resultOfCAC);
			Message request = new Message(MessageType.REQUEST, payload);
			connect.sendMessage(request);
		}
	}
	
	public void handleRequestMessage(Connection connect, int pieceNumber) throws Exception{
		byte[] pieceIndex = Util.IntToByte(pieceNumber);
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
		int pieceNum = Util.Byte2Int(payLoad);
		PeerInfo peerInfo = connect.getPeerInfo();
		BitfieldManager.getInstance().updateBitfield(peerInfo, pieceNum);			//update Bitfield
		int resultOfCAC;
		resultOfCAC = BitfieldManager.getInstance().compareAndchoose(connect.getPeerInfo());
		if(resultOfCAC == -1) {
			Message notInterested = new Message(MessageType.NOT_INTERESTED, null);	//Not interested
			connect.sendMessage(notInterested);
		}
		else {
			byte[] payload = Util.IntToByte(resultOfCAC);
			Message request = new Message(MessageType.REQUEST, payload);			//request random one
			connect.sendMessage(request);
			Message have = new Message(MessageType.HAVE, payload);					//send have Piece number
			connect.sendMessage(have);
		}
		
	}
	
}
