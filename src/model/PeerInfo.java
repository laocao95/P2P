package model;
import java.io.*;
import java.text.*;
import java.util.*;

import controller.ArgReader;

public class PeerInfo {
	private int peerID;
	private String host;
	private int port;
	private Boolean hasFile;
	private File file;
	private String filePath;
	BufferedWriter writer;
	
	public PeerInfo(int peerID, String host, int port, Boolean hasFile) {
		this.peerID = peerID;
		this.host = host;
		this.port = port;
		this.hasFile = hasFile;
		// TODO Auto-generated constructor stub
	}
	public int getId() {
		return peerID;
	}
	public String getHost() {
		return host;
	}
	public int getPort() {
		return port;
	}
	public Boolean getHasFile() {
		return hasFile;
	}
	public File getFile(){
		return file;
	}
	public void setFile() throws IOException{
		filePath = "log_peer_" + peerID + ".log";		//file address
		file = new File(filePath);
		writer = new BufferedWriter(new FileWriter(filePath, false));
	}
	public void writeLog(String logInfo, PeerInfo opPeerInfo) throws IOException{
		int opPeerID = opPeerInfo.getId();
		System.out.println("start write log.");
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		if(logInfo == "TCPconnection"){
			//read timer
			System.out.println(dateFormat.format(date)); 
			writer.write("[" + dateFormat.format(date) + "]");
			//write log
			writer.write(": Peer [" + peerID + "] makes a connection to Peer [" + opPeerID + "].");
			writer.newLine();
			writer.close();
		}
		else if(logInfo == "changeOfPreferredNeighbors"){
			writer.write("[" + dateFormat.format(date) + "]");
			//writer.write(": Peer [" + peerID + "] has the preferred neighbors [" + MyTimer + "].");
		}
		else if(logInfo == "changeOfOptimisticallyUnchokedNeighbor"){
			
		}
		else if(logInfo == "unchoking"){
			writer.write("[" + dateFormat.format(date) + "]");
			writer.write(": Peer [" + peerID + "] is unchoked by [" + opPeerID + "].");
			writer.newLine();
			writer.close();
			
		}
		else if(logInfo == "choking"){
			writer.write("[" + dateFormat.format(date) + "]");
			writer.write(": Peer [" + peerID + "] is choked by [" + opPeerID + "].");
			writer.newLine();
			writer.close();
		}
		else if(logInfo == "receivingHaveMessage"){
			//read timer
			System.out.println(dateFormat.format(date)); 
			writer.write("[" + dateFormat.format(date) + "]");
			//write log
			writer.write(": Peer [" + peerID + "] received the ‘have’ message from Peer [" + opPeerID + "].");
			writer.newLine();
			writer.close();
		}
		else if(logInfo == "receivingInterestedMessage"){
			//read timer
			System.out.println(dateFormat.format(date)); 
			writer.write("[" + dateFormat.format(date) + "]");
			//write log
			writer.write(": Peer [" + peerID + "] received the ‘interested’ message from Peer [" + opPeerID + "].");
			writer.newLine();
			writer.close();
		}
		else if(logInfo == "receivingNotInterestedMessage"){
			//read timer
			System.out.println(dateFormat.format(date)); 
			writer.write("[" + dateFormat.format(date) + "]");
			//write log
			writer.write(": Peer [" + peerID + "] received the ‘not interested’ message from Peer [" + opPeerID + "].");
			writer.newLine();
			writer.close();
		}
		else if(logInfo == "downloadingAPiece"){
			//read timer
			System.out.println(dateFormat.format(date)); 
			writer.write("[" + dateFormat.format(date) + "]");
			//write log
			//writer.write(": Peer [" + peerID + "] has downloaded the piece [" + +"] from Peer [" + opPeerID + "].");
			//writer.newLine();
			writer.close();
		}
		else if(logInfo == "completionOfDownload"){
			//read timer
			System.out.println(dateFormat.format(date)); 
			writer.write("[" + dateFormat.format(date) + "]");
			//write log
			writer.write(": Peer [" + peerID + "] has downloaded the complete file.");
			writer.newLine();
			writer.close();
		}
	}
}
