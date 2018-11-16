package controller;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import custom.Config.*;
import model.PeerInfo;

public class Log {
	private String filePath;
	private BufferedWriter writer;
	private PeerInfo myInfo;
	private static class SingletonHolder {
		public final static Log instance = new Log();
	}
	private Log() {
		myInfo = PeerInfoManager.getInstance().getMyInfo();
		filePath = "log_peer_" + myInfo.getId() + ".log";		//file address
		//set append to false to make sure over write the file when restart
		try {
			writer = new BufferedWriter(new FileWriter(filePath, false));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static Log getInstance() {
		return SingletonHolder.instance;
	}
	public void writeLog(LogType logType, PeerInfo opPeer, Object args) {
		try {
			writer = new BufferedWriter(new FileWriter(filePath, true));
			System.out.println("start write log.");
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			switch(logType){
				case TCPConnection : {
					//read timer
					System.out.println(dateFormat.format(date)); 
					writer.write("[" + dateFormat.format(date) + "]");
					//write log
					writer.write(": Peer [" + myInfo.getId() + "] makes a connection to Peer [" + opPeer.getId() + "].");
					writer.newLine();
					writer.close();
				}
				break;
				case ChangeOfPreferredNeighbor :{
					writer.write("[" + dateFormat.format(date) + "]");
					String preferredNeighbour = (String) args;
					writer.write(": Peer [" + myInfo.getId() + "] has the preferred neighbors [" + preferredNeighbour + "].");
				}
				break;
				case ChangeOfOptUnchokedNeighbor :{
					writer.write("[" + dateFormat.format(date) + "]");
					writer.write(": Peer [" + myInfo.getId() + "] has the optimistically unchoked neighbor [Optimistically unchoked neighbour " + opPeer.getId() + "].");
				}
				break;
				case Unchoking :{
					writer.write("[" + dateFormat.format(date) + "]");
					writer.write(": Peer [" + myInfo.getId() + "] is unchoked by [" + opPeer.getId() + "].");
					writer.newLine();
					writer.close();
				}
				break;
				case Choking :{
					writer.write("[" + dateFormat.format(date) + "]");
					writer.write(": Peer [" + myInfo.getId() + "] is choked by [" + opPeer.getId() + "].");
					writer.newLine();
					writer.close();
				}
				break;
				case ReceivingHaveMessage:{
					//read timer
					System.out.println(dateFormat.format(date)); 
					writer.write("[" + dateFormat.format(date) + "]");
					//write log
					writer.write(": Peer [" + myInfo.getId() + "] received the 'have' message from Peer [" + opPeer.getId() + "].");
					writer.newLine();
					writer.close();
				}
				break;
				case ReceivingInterestedMessage:{
					//read timer
					System.out.println(dateFormat.format(date)); 
					writer.write("[" + dateFormat.format(date) + "]");
					//write log
					writer.write(": Peer [" + myInfo.getId() + "] received the 'interested' message from Peer [" + opPeer.getId() + "].");
					writer.newLine();
					writer.close();
				}
				break;
				case ReceivingNotInterestedMessage:{
					//read timer
					System.out.println(dateFormat.format(date)); 
					writer.write("[" + dateFormat.format(date) + "]");
					//write log
					writer.write(": Peer [" + myInfo.getId() + "] received the 'not interested' message from Peer [" + opPeer.getId() + "].");
					writer.newLine();
					writer.close();
				}
				break;
				case DownloadingAPiece :{
					//read timer
					System.out.println(dateFormat.format(date)); 
					writer.write("[" + dateFormat.format(date) + "]");
					//write log
					Integer pieceNum = (Integer)args;
					writer.write(": Peer [" + myInfo.getId() + "] has downloaded the piece [" + pieceNum +"] from Peer [" + opPeer.getId() + "].");
					writer.newLine();
					writer.close();
				}
				break;
				case CompletionOfDownload :{
					//read timer
					System.out.println(dateFormat.format(date)); 
					writer.write("[" + dateFormat.format(date) + "]");
					//write log
					writer.write(": Peer [" + myInfo.getId() + "] has downloaded the complete file.");
					writer.newLine();
					writer.close();
				}
				break;
			}
		} catch(Exception e) {
				e.printStackTrace();
		}

	}
}
