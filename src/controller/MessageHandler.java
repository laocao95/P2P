package controller;

import model.*;
import custom.*;

import java.util.List;
import custom.Config.*;

public class MessageHandler {
	private Connection connect;
	public MessageHandler(Connection connection) {
		// TODO Auto-generated constructor stub
		connect = connection;
	}
	public void handleHandshakeMessage(Message message) throws Exception{
		//check header
		
		String header = "P2PFILESHARINGPROJ";

		HandShakeMessage handShakeMessage = (HandShakeMessage)message;
		
		if (!handShakeMessage.getHeader().equals(header)) {
			System.out.println("error header is " + handShakeMessage.getHeader());
			throw new Exception("error handshake header");
		}
		//opPeer is the handshake initiator
		if (connect.getOpPeer() == null) {
			int peerId = handShakeMessage.getPeerID();
			PeerInfo peerInfo = PeerInfoManager.getInstance().getPeerInfoById(peerId);
			connect.setOpPeer(peerInfo);
			connect.setReceivedHandShake();
			Log.getInstance().writeLog(LogType.TCPConnection, peerInfo, null);
			System.out.println("receive handshake from " + connect.getOpPeer().getId());

			//send handshake
			sendHandShakeMessage();
			//send bitfield
			sendBitfieldMessage();
		} else {
			//I am the handshake initiator, no need to send handshake again
			connect.setReceivedHandShake();
			System.out.println("receive handshake from " + connect.getOpPeer().getId());
		}
	}
	
	public void sendHandShakeMessage() throws Exception{
		
		String header = "P2PFILESHARINGPROJ";
		
		HandShakeMessage message = new HandShakeMessage(header, PeerInfoManager.getInstance().getMyInfo().getId());
		
		connect.sendMessage(message);
		
		connect.setSendedHandShake();
		
		System.out.println("send handshake message to " + connect.getOpPeer().getId());
	}
	public void sendBitfieldMessage() throws Exception{
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
	
	public void handleBitFieldMessage(Message message) throws Exception{
		Message bitField = (Message)message;
		PeerInfo peerInfo = connect.getOpPeer();
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
	
	public void handleHaveMessage(Message message) throws Exception{
		Message have = (Message)message;
		byte[] payLoad = have.getPayload();
		int pieceNum = Util.Byte2Int(payLoad);
		PeerInfo peerInfo = connect.getOpPeer();
		//update opPeer bitfield
		BitfieldManager.getInstance().updateBitfield(peerInfo, pieceNum);
		//check opPeer and me receive all piece
		Log.getInstance().writeLog(LogType.TestLog, null, "receive have message from " + peerInfo.getId() + " for piece " + pieceNum);
		if (BitfieldManager.getInstance().isAllReceived(peerInfo) && 
				BitfieldManager.getInstance().isAllReceived(PeerInfoManager.getInstance().getMyInfo())) {
			Log.getInstance().writeLog(LogType.TestLog, null, "receive all have " + peerInfo.getId());
			connect.setFinish();
		} else {
			if (BitfieldManager.getInstance().comparePeerInfo(peerInfo)){
				Message interested = new Message(MessageType.INTERESTED, null);
				connect.sendMessage(interested);
			}
			else{
				Message notInterested = new Message(MessageType.NOT_INTERESTED, null);
				connect.sendMessage(notInterested);
			}
		}
	}
	
	public void handleUnchokedMessage(Message message) throws Exception{
		int resultOfCAC;
		resultOfCAC = BitfieldManager.getInstance().compareAndchoose(connect.getOpPeer());
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
	
	public void handleInterestedMessage() throws Exception{
		//Message interest = (Message)message;
		connect.setInterested();
	}
	
	public void handleUninterestedMessage() throws Exception{
		//Message notInterest = (Message)message;
		connect.setNotInterested();
	}
	
	public void handleRequestMessage(Message message) throws Exception{
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
		System.out.println("Send piece.");
	}
	
	public void handlePieceMessage(Message message, List<Connection> connectionList) throws Exception{
		Message piece = (Message)message;
		byte[] payload = piece.getPayload();
		byte[] pieceIndex = new byte[4];
		byte[] pieceContent = new byte[payload.length - 4];
		System.arraycopy(payload, 0, pieceIndex, 0, 4);
		System.arraycopy(payload, 4, pieceContent, 0, pieceContent.length);
		int pieceNum = Util.Byte2Int(pieceIndex);
		FileManager.getInstance().write(pieceNum, pieceContent);
		PeerInfo peerInfo = PeerInfoManager.getInstance().getMyInfo();
		BitfieldManager.getInstance().updateBitfield(peerInfo, pieceNum);			//update Bitfield
		Log.getInstance().writeLog(LogType.DownloadingAPiece, connect.getOpPeer(), pieceNum);
		
		//check if finished
		if (BitfieldManager.getInstance().isAllReceived(peerInfo)) {
			//write complete log
			FileManager.getInstance().renameTemp();
			Log.getInstance().writeLog(LogType.CompletionOfDownload, connect.getOpPeer(), null);
			//broadcast not interest
			for (Connection connection : connectionList) {
				if (connection.getSendedHandShake() && !connection.getFinish()) {
					Message notInterested = new Message(MessageType.NOT_INTERESTED, null);	//Not interested
					connection.sendMessage(notInterested);
				}
			}
		}
		//broadcast have message
		for (Connection connection : connectionList) {
			//ensure peer send handshake first and connection is still running
			if (connection.getSendedHandShake() && !connection.getFinish()) {
				Log.getInstance().writeLog(LogType.TestLog, null, "send have to " + connect.getOpPeer().getId() + " for piece " + pieceNum);
				Message have = new Message(MessageType.HAVE, payload);					//send have Piece number
				connection.sendMessage(have);
				//Log.getInstance().writeLog(LogType.TestLog, null, "send have to " + connect.getOpPeer().getId() + "for piece " + pieceNum);
			}
		}
		//check whether close socket
		if (BitfieldManager.getInstance().isAllReceived(peerInfo)) {
			//if opPeer receive all piece
			for (Connection connection : connectionList) {
				if (BitfieldManager.getInstance().isAllReceived(connection.getOpPeer())) {
					Log.getInstance().writeLog(LogType.TestLog, null, "enter close socket " + connect.getOpPeer().getId());
					//System.out.println("enter close socket " + connect.getOpPeer().getId());
					connection.setFinish();
				}
			}
		}
		else if (connect.getpeerChokeMe() == false){
			int resultOfCAC;
			resultOfCAC = BitfieldManager.getInstance().compareAndchoose(connect.getOpPeer());
			if(resultOfCAC == -1) {
				Message notInterested = new Message(MessageType.NOT_INTERESTED, null);	//Not interested
				connect.sendMessage(notInterested);
			}
			else {
				byte[] newPayload = Util.IntToByte(resultOfCAC);
				Message request = new Message(MessageType.REQUEST, newPayload);			//request random one
				connect.sendMessage(request);
			}
		}
	}
}
