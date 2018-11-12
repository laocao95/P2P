import java.net.*;
import controller.*;
import java.util.*;
import model.*;
public class peerProcess {
	private ServerSocket serverSocket;
	private List<Connection> peerConnectionList;
	private PeerInfo serverInfo;
	
	public peerProcess(int id) throws Exception {
		
		peerConnectionList = new ArrayList<>();
		
		serverInfo = PeerInfoManager.getInstance().getPeerInfoById(id);
		PeerInfoManager.getInstance().setMyInfo(serverInfo);
		//connect to peers before this server
		connectToBeforePeer();
		
		//start timer
		MyTimer mytimer = new MyTimer(peerConnectionList);
		mytimer.startTimer();
		
		//wait for peers connect later
		try {
			
			serverSocket = new ServerSocket(serverInfo.getPort());
			for (int i = 0; i < PeerInfoManager.getInstance().getPeersAfter(serverInfo).size(); i++) {
				
				Socket socket = serverSocket.accept();
				
				peerConnectionList.add(new Connection(socket));
			}
			
			//
		
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			serverSocket.close();
		}
	}
	
	public void connectToBeforePeer() {
		
		try {
			for (PeerInfo peer : PeerInfoManager.getInstance().getPeersBefore(serverInfo)) {
				Socket socket = new Socket(peer.getHost(), peer.getPort());
				peerConnectionList.add(new Connection(socket, peer));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.out.println("begin start server");
		try {
			new peerProcess(Integer.parseInt(args[0]));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
