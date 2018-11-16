package model;
import java.io.*;
import java.net.*;
import java.util.*;
import controller.*;
import custom.Util;
import custom.Config.*;
public class Connection extends Thread{
	private Socket socket;
	private PeerInfo opPeerInfo = null;
	private InputStream inputStream;
	private OutputStream outputStream;
	private boolean correspondingPeersCompleted = false;
	private boolean receivedHandShake = false;
	private boolean sendedHandShake = false;
	private boolean peerChokeMe = true;
	private boolean peerInterestMe = false;
	private int downloadingNumOfPeriod = 0;
	private peerProcess processController;
	private Log log;
	private MessageHandler messageHandler;

	public Connection(Socket s, PeerInfo info, peerProcess controller) {
		try {
			socket = s;
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			processController = controller;
			log = Log.getInstance();
			messageHandler = new MessageHandler(this);
			if (info != null) {
				opPeerInfo = info;
				log.writeLog(LogType.TCPConnection, opPeerInfo, null);
			}
			super.start();
		} catch(Exception e) {
			e.printStackTrace();
		}

	}	
	@Override
	public void run() {
		try {
			if (opPeerInfo != null) {
				messageHandler.sendHandShakeMessage();
				messageHandler.sendBitfieldMessage();
			}
			
			//need to implement stop condition
			while(!correspondingPeersCompleted) {
				//read a message
				Message message = readMessage();
				MessageType type = message.getType();
				switch(type) {
					case HANDSHAKE: {
						messageHandler.handleHandshakeMessage(message);
					}
					break;
					case CHOKE: {
						peerChokeMe = true;
						System.out.println(opPeerInfo.getId() + " choke me");
						log.writeLog(LogType.Choking, opPeerInfo, null);
					}
					break;
					case UNCHOKE: {
						peerChokeMe = false;
						messageHandler.handleUnchokedMessage(message);
						System.out.println(opPeerInfo.getId() + " unchoke me");
						log.writeLog(LogType.Unchoking, opPeerInfo, null);
					}
					break;
					case INTERESTED: {
						System.out.println(opPeerInfo.getId() + " interest me");
						peerInterestMe = true;
						log.writeLog(LogType.ReceivingInterestedMessage, opPeerInfo, null);
					}
					break;
					case NOT_INTERESTED: {
						System.out.println(opPeerInfo.getId() + " notInterest me");
						peerInterestMe = false;
						log.writeLog(LogType.ReceivingNotInterestedMessage, opPeerInfo, null);
					}
					break;
					case HAVE: {
						log.writeLog(LogType.ReceivingHaveMessage, opPeerInfo, null);
						messageHandler.handleHaveMessage(message);
					}
					break;
					case BITFIELD: {
						messageHandler.handleBitFieldMessage(message);
					}
					break;
					case REQUEST: {
						//when receive request, check if is choked
						System.out.println("receive request from " + opPeerInfo.getId());
						messageHandler.handleRequestMessage(message);							
					}
					break;
					case PIECE: {
						downloadingNumOfPeriod++;
						System.out.println("receive piece from " + opPeerInfo.getId());
						messageHandler.handlePieceMessage(message, processController.getConnectionList());
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
	public void setOpPeer(PeerInfo info) {
		opPeerInfo = info;
	}
	
	public PeerInfo getOpPeer() {
		return opPeerInfo;
	}

	public void sendMessage(Message message) {
		try {
			//ensure not to send message after socket close
			if (correspondingPeersCompleted) {
				return;
			}
			outputStream.write(message.toBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	public boolean getReceivedHandShake() {
		return receivedHandShake;
	}
	public int getDownloadingNumOfPeriod() {
		return downloadingNumOfPeriod;
	}
	public void resetDownloadingNum() {
		downloadingNumOfPeriod = 0;
	}
	public boolean getInterestedFlag() {
		return peerInterestMe;
	}
	public void setInterested(){
		peerInterestMe = true;
	}
	public void setNotInterested(){
		peerInterestMe = false;
	}
	public void setSendedHandShake(){
		sendedHandShake = true;
	}
	public boolean getSendedHandShake(){
		return sendedHandShake;
	}
	public void setFinish() {
		correspondingPeersCompleted = true;
	}
	public boolean getFinish() {
		return correspondingPeersCompleted;
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
			System.out.println("waiting for coming message from " + opPeerInfo.getId());

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
			
			System.out.println("typeIndex" + typeIndex);
			
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
	
	public boolean getpeerChokeMe(){
		return peerChokeMe;
	}
	
	public void close() throws Exception {
		Thread.sleep(500);
		socket.close();
	}
}
