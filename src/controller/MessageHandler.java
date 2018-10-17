package controller;

import model.*;

public class MessageHandler {
	private static MessageHandler instance;
	private MessageHandler() {
		// TODO Auto-generated constructor stub
	}
	
	public static MessageHandler getInstance() {
		if (instance == null) {
			instance = new MessageHandler();
		}
		return instance;
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
}