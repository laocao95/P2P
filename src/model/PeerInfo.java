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
		this.filePath = "log_peer_" + this.peerID + ".log";		//file address
		this.file = new File(filePath);
		writer = new BufferedWriter(new FileWriter(this.filePath));
	}
	public void writeLog(String logInfo, PeerInfo opPeerInfo) throws IOException{
		int opPeerID = opPeerInfo.getId();
		if(logInfo == "TCPconnection"){
			//BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath));
			//read timer
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			System.out.println(dateFormat.format(date)); 
			writer.write("[" + dateFormat.format(date) + "]");
			writer.write(": Peer [" + peerID + "] makes a connection to Peer [" + opPeerID + "]. ");
		}
		else if(logInfo == "changeOfPreferredNeighbors"){
			
		}
		else if(logInfo == "changeOfOptimisticallyUnchokedNeighbor"){
			
		}
		else if(logInfo == "unchoking"){
			
		}
		else if(logInfo == "choking"){
			
		}
		else if(logInfo == "receivingHaveMessage"){
			
		}
		else if(logInfo == "receivingInterestedMessage"){
			
		}
		else if(logInfo == "receivingNotInterestedMessage"){
			
		}
		else if(logInfo == "downloadingAPiece"){
			
		}
		else if(logInfo == "completionOfDownload"){
			
		}
	}
}
