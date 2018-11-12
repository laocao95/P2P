package model;
import java.io.*;
import java.net.*;
import java.util.*;
import controller.*;
import custom.Util;
import custom.Config.*;

public class Connection extends Thread{
	private Socket socket;
	private PeerInfo peerInfo = null;
	private InputStream inputStream;
	private OutputStream outputStream;
	private Boolean correspondingPeersCompleted = false;
	private Boolean receivedHandShake = false;
	private Boolean serverChoke = false;
	private Boolean clientInterest = false;
	
	
	//check handshake
	public Connection(Socket s) {
		try {
			socket = s;
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
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
			super.start();
		} catch(Exception e) {
			e.printStackTrace();
		}

	}	
	@Override
	public void run() {
		try {
			
			if (peerInfo != null) {

				MessageHandler.getInstance().sendHandShakeMessage(this);
			}
			//need to add a Controller.sendBitfiedMessage()
			
			//need to implement stop condition
			while(!correspondingPeersCompleted) {
				//read a message
				Message message = readMessage();
				MessageType type = message.getType();
				switch(type) {
					case HANDSHAKE: {
						MessageHandler.getInstance().handleHandshakeMessage(this, message);
					}
					break;
					case CHOKE: {
						serverChoke = true;
						System.out.println("[Time]: Peer [peer_ID 1] is choked by [peer_ID 2].");
					}
					break;
					case UNCHOKE: {
						serverChoke = false;
						MessageHandler.getInstance().handleUnchokedMessage(this, message);
					}
					break;
					case INTERESTED: {
						clientInterest = true;
					}
					break;
					case NOT_INTERESTED: {
						clientInterest = false;
					}
					break;
					case HAVE: {
						
					}
					break;
					case BITFIELD: {
						MessageHandler.getInstance().handleBitFieldMessage(this, message);
					}
					break;
					case REQUEST: {
						//when receive request, check if is choked
						if (serverChoke == true){
							//don't reply the request
						}
						else
						{
							
							//send requested piece
						}
								
					}
					break;
					case PIECE: {
						
					}
					break;
					default: {
						throw new Exception("Message type error");
					}
				}
				//break;
			}
			//two corresponding peers have files, close socket
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

	public void sendMessage(Message message) {
		
		new Thread() {
			public void run() {
				try {
					outputStream.write(message.toBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	public OutputStream getOutputStream() {
		return outputStream;
	}
	public void setReceivedHandShake() {
		receivedHandShake = true;
	}
	public Boolean getReceivedHandShake() {
		return receivedHandShake;
	}
	
	public Message readMessage() throws Exception{
		
		if (!receivedHandShake) {
			byte[] message = new byte[32];
			try {
				while (inputStream.available() < 32) {
					Thread.sleep(20);
				}
				inputStream.read(message);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			byte[] headerBytes = Arrays.copyOfRange(message, 0, 18);
			byte[] peerIDBytes = Arrays.copyOfRange(message, 28, 32);
			String header = new String(headerBytes);
			int peerID = Util.Byte2Int(peerIDBytes);
			return new HandShakeMessage(header, peerID);
			
		} else {
			System.out.println("waiting for coming message from " + peerInfo.getId());

			while (inputStream.available() < 5) {
				//wait for header and length byte
				Thread.sleep(20);
			}
			byte[] typeAndLength = new byte[5];
			inputStream.read(typeAndLength);
			
			int typeIndex = typeAndLength[4] - '0';
			
			MessageType[] typeArray = MessageType.values();
			MessageType type = typeArray[typeIndex];
			
			byte[] lengthBytes = Arrays.copyOfRange(typeAndLength, 0, 4);
			int length = Util.Byte2Int(lengthBytes);
			
			Message message;
			if (length == 1) {
				message = new Message(type, null);
			} else {
				//length contain message type field which is 1
				while (inputStream.available() < length - 1) {
					Thread.sleep(20);
				}
				byte[] payload = new byte[length - 1];
				inputStream.read(payload);
				message = new Message(type, payload);

			}
			return message;
		}

	}
	public void close() throws IOException {
		socket.close();
	}
}
