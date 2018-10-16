package model;
import java.io.*;
import java.net.*;
import java.util.*;
import controller.*;
import config.Config.MessageType;

public class Connection extends Thread{
	private Socket socket;
	private PeerInfo peerInfo = null;
	private InputStream inputStream;
	private OutputStream outputStream;
	//private FileManager
	private Boolean allPeerFinish = false;
	
	
	//check handshake
	public Connection(Socket s) {
		try {
			socket = s;
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			handShakeProcess();
			super.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public Connection(Socket s, PeerInfo info) {
		try {
			socket = s;
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			peerInfo = info;
			handShakeProcess();
			//bitfield need to implement
			//start a tread
			super.start();
		} catch(Exception e) {
			e.printStackTrace();
		}

	}	
	@Override
	public void run() {
		try {
			//need to implement stop condition
			while(!allPeerFinish) {
				//read a message
				Message message = reader();
				MessageType type = message.getType();
				switch(type) {
					case CHOKE: {
						
					}
					break;
					case UNCHOKE: {
						
					}
					break;
					case INTERESTED: {
						
					}
					break;
					case NOT_INTERESTED: {
						
					}
					break;
					case HAVE: {
						
					}
					break;
					case BITFIELD: {
						
					}
					break;
					case REQUEST: {
						
					}
					break;
					case PIECE: {
						
					}
					break;
					default: {
						
					}
				}
				//break;
			}
			
			
			close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	public void setPeerInfo(PeerInfo info) {
		peerInfo = info;
	}
	
	public PeerInfo getPeerInfo() {
		return peerInfo;
	}

	public void sendMessage(byte[] message) {
		
		new Thread() {
			public void run() {
				try {
					outputStream.write(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	//initialize and send handshake and bitfield
	public void handShakeProcess() {
		try {
			if (peerInfo == null) {
				//wait for handshake and send handshake
				HandShakeController.getInstance().handelHandshakeMessage(this);
				HandShakeController.getInstance().sendHandShakeMessage(this);
			} else {
				//send handshake first and wait for handshake
				HandShakeController.getInstance().sendHandShakeMessage(this);
				HandShakeController.getInstance().handelHandshakeMessage(this);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	public Message reader() throws Exception{
		
		System.out.println("waiting for coming message from " + peerInfo.getId());
		
		while (inputStream.available() < 5) {
			//wait for header hand length byte
			Thread.sleep(100);
		}
		byte[] typeAndLength = new byte[5];
		inputStream.read(typeAndLength);
		
		int type = typeAndLength[0];
		byte[] lengthByte = Arrays.copyOfRange(typeAndLength, 1, 5);
		int length = Integer.valueOf(new String(lengthByte));
		
		while (inputStream.available() < length) {
			Thread.sleep(100);
		}
		
		byte[] payload = new byte[length];
		inputStream.read(payload);
		
		Message m = new Message(type, length, payload);
		return m;
	}
	public void close() throws IOException {
		socket.close();
	}
}
