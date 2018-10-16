package controller;
import java.io.*;
import java.util.Arrays;

import model.*;

public class HandShakeController {
	private static HandShakeController instance;
	private HandShakeController() {
		// TODO Auto-generated constructor stub
	}
	
	public static HandShakeController getInstance() {
		if (instance == null) {
			instance = new HandShakeController();
		}
		return instance;
	}

	public void handelHandshakeMessage(Connection connect) throws Exception{
		byte[] message = new byte[32];
		InputStream inputStream = connect.getInputStream();
		try {
			while (inputStream.available() < 32) {
				Thread.sleep(20);
			}
			inputStream.read(message);
		} catch(Exception e) {
			e.printStackTrace();
		}
		//check header
		String header = "P2PFILESHARINGPROJ";
		if (!Arrays.equals(header.getBytes(), Arrays.copyOfRange(message, 0, 18))) {
			throw new Exception("error handshake header");
		}
		byte[] peerIdByte = Arrays.copyOfRange(message, 28, 32);
		int peerId = Integer.valueOf(new String(peerIdByte));
		
		PeerInfo peerInfo = PeerInfoController.getInstance().getPeerInfoById(peerId);
		connect.setPeerInfo(peerInfo);
		
		System.out.println("receive handshake from " + peerInfo.getId());
	}
	
	public void sendHandShakeMessage(Connection connect) throws Exception{
		
		byte[] message = new byte[32];

		String header = "P2PFILESHARINGPROJ";
		byte[] headerBytes = header.getBytes();
		
		byte[] peerIdBytes = String.valueOf(PeerInfoController.getInstance().getMyInfo().getId()).getBytes();
		
		System.arraycopy(headerBytes, 0, message, 0, headerBytes.length);
		System.arraycopy(peerIdBytes, 0, message, 28, peerIdBytes.length);
		
		connect.sendMessage(message);
		
		System.out.println("send handshake message to " + connect.getPeerInfo().getId());
	}
}
