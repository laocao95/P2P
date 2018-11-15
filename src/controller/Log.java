package controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import model.PeerInfo;

public class Log {
	private String filePath;
	private BufferedWriter writer;
	private PeerInfo myInfo;
	private PeerInfo opPeer = null;
	
	public Log() {
		myInfo = PeerInfoManager.getInstance().getMyInfo();
		filePath = "log_peer_" + myInfo.getId() + ".log";		//file address
		//set append to false to make sure over write the file when restart
		try {
			writer = new BufferedWriter(new FileWriter(filePath, false));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void setOpPeer(PeerInfo opPeer) {
		this.opPeer = opPeer;
	}
	public void writeLog(String logInfo) throws IOException{
		if (opPeer == null) {
			System.out.println("opPeer is null, can't not write the log");
			return;
		}
		writer = new BufferedWriter(new FileWriter(filePath, true));
		System.out.println("start write log.");
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		if(logInfo.equals("TCPconnection")){
			//read timer
			System.out.println(dateFormat.format(date)); 
			writer.write("[" + dateFormat.format(date) + "]");
			//write log
			writer.write(": Peer [" + myInfo.getId() + "] makes a connection to Peer [" + opPeer.getId() + "].");
			writer.newLine();
			writer.close();
		}
		else if(logInfo.equals("changeOfPreferredNeighbors")){
			writer.write("[" + dateFormat.format(date) + "]");
			//writer.write(": Peer [" + peerID + "] has the preferred neighbors [" + MyTimer + "].");
		}
		else if(logInfo.equals("changeOfOptimisticallyUnchokedNeighbor")){
			
		}
		else if(logInfo.equals("unchoking")){
			writer.write("[" + dateFormat.format(date) + "]");
			writer.write(": Peer [" + myInfo.getId() + "] is unchoked by [" + opPeer.getId() + "].");
			writer.newLine();
			writer.close();
			
		}
		else if(logInfo.equals("choking")){
			writer.write("[" + dateFormat.format(date) + "]");
			writer.write(": Peer [" + myInfo.getId() + "] is choked by [" + opPeer.getId() + "].");
			writer.newLine();
			writer.close();
		}
		else if(logInfo.equals("receivingHaveMessage")){
			//read timer
			System.out.println(dateFormat.format(date)); 
			writer.write("[" + dateFormat.format(date) + "]");
			//write log
			writer.write(": Peer [" + myInfo.getId() + "] received the 'have' message from Peer [" + opPeer.getId() + "].");
			writer.newLine();
			writer.close();
		}
		else if(logInfo.equals("receivingInterestedMessage")){
			//read timer
			System.out.println(dateFormat.format(date)); 
			writer.write("[" + dateFormat.format(date) + "]");
			//write log
			writer.write(": Peer [" + myInfo.getId() + "] received the 'interested' message from Peer [" + opPeer.getId() + "].");
			writer.newLine();
			writer.close();
		}
		else if(logInfo.equals("receivingNotInterestedMessage")){
			//read timer
			System.out.println(dateFormat.format(date)); 
			writer.write("[" + dateFormat.format(date) + "]");
			//write log
			writer.write(": Peer [" + myInfo.getId() + "] received the 'not interested' message from Peer [" + opPeer.getId() + "].");
			writer.newLine();
			writer.close();
		}
		else if(logInfo.equals("downloadingAPiece")){
			//read timer
			System.out.println(dateFormat.format(date)); 
			writer.write("[" + dateFormat.format(date) + "]");
			//write log
			//writer.write(": Peer [" + peerID + "] has downloaded the piece [" + +"] from Peer [" + opPeerID + "].");
			//writer.newLine();
			writer.close();
		}
		else if(logInfo.equals("completionOfDownload")){
			//read timer
			System.out.println(dateFormat.format(date)); 
			writer.write("[" + dateFormat.format(date) + "]");
			//write log
			writer.write(": Peer [" + myInfo.getId() + "] has downloaded the complete file.");
			writer.newLine();
			writer.close();
		}
	}
	
}
