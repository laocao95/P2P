package controller;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import custom.Config.LogType;
import custom.Config.MessageType;
import model.Connection;
import model.Message;

public class MyTimer extends Thread{
	private List<Connection> connectionList;
	private List<Connection> preferedList;
	private Connection optimisticPeer;
	private int unchokingInterval;
	private int optimisticUnchokingInterval;
	private long lastUnchokingTime;
	private long lastOptimisticUnchokingTime;
	private boolean startFlag;
	private int maxNeighborNum;
	
	public MyTimer(List<Connection> connectionList) {
		this.connectionList = connectionList;
		unchokingInterval = ArgReader.getInstance().getUnchokingInterval() * 1000;
		optimisticUnchokingInterval = ArgReader.getInstance().getOptimisticUnchokingInterval() * 1000;
		preferedList = new ArrayList<>();
		optimisticPeer = null;
		lastUnchokingTime = 0;
		lastOptimisticUnchokingTime = 0;
		maxNeighborNum = ArgReader.getInstance().getNumberOfPreferredNeighbors();
		startFlag = true;
	}
	public void startTimer() {
		startFlag = true;
		super.start();
	}
	public void stopTimer() {
		startFlag = false;
	}
	public List<Connection> getPreferedList() {
		return preferedList;
	}
	
	@Override
	public void run() {
		while(startFlag) {
			//first machine should wait for connection and then start timer.
			int connectionSize = connectionList.size();
			System.out.print("");
			if (connectionSize == 0) {
				continue;
			}
			long currentMillis = System.currentTimeMillis();
			List<Connection> runningConnection = new ArrayList<>();
			
			
			for (int i = 0; i < connectionSize; i++) {
				if (!connectionList.get(i).getFinish()) {
					runningConnection.add(connectionList.get(i));
				}
			}
			
			//if no connection is running, and all peers already login, stop timer and exit();
			if (runningConnection.size() == 0 && connectionSize == PeerInfoManager.getInstance().getSize() - 1) {
				Log.getInstance().writeLog(LogType.TestLog, null, "close program. connectionSize" + connectionSize);
				System.exit(0);
			}
			
			//select preferred neighbor
			if (currentMillis - lastUnchokingTime > unchokingInterval) {
				//reset timer
				lastUnchokingTime = currentMillis;
				List<Connection> interestedList = new ArrayList<>();
				List<Connection> newPreferedList = new ArrayList<>();
				//select interest list
				
				for (Connection connection : runningConnection) {
					if (connection.getInterestedFlag()) {
						interestedList.add(connection);
					}
				}
				//check if has entire file
				if (BitfieldManager.getInstance().isAllReceived(PeerInfoManager.getInstance().getMyInfo())) {
					//randomly select k new prefer neighbor
					int k = maxNeighborNum > interestedList.size() ? interestedList.size() : maxNeighborNum;
					Collections.shuffle(interestedList);
					for (int i = 0; i < k; i++) {
						newPreferedList.add(interestedList.get(i));
					}
					
				} else {
					//don't have entire file, select new prefer list
					if (interestedList.size() > 0) {
						if (interestedList.size() <= maxNeighborNum) {
							newPreferedList = interestedList;
						} else {
							//sort by downloadingNum
							Collections.sort(interestedList, new Comparator<Connection>() {
								@Override
								public int compare(Connection c1, Connection c2) {
									return c2.getDownloadingNumOfPeriod() - c1.getDownloadingNumOfPeriod();
								}
							});
							//add k fast
							for (int i = 0; i < maxNeighborNum; i++) {
								newPreferedList.add(interestedList.get(i));
							}
						}
					}
				}
				for (Connection connection : newPreferedList) {
					if (!preferedList.contains(connection)) {
						//not in previous preferredList, send unchokeMessage
						connection.sendMessage(new Message(MessageType.UNCHOKE, null));
					}
				}
				
				for (Connection connection : runningConnection) {
					if (!newPreferedList.contains(connection) && connection.getSendedHandShake()) {
						//not in new preferredList, send chokeMessage
						connection.sendMessage(new Message(MessageType.CHOKE, null));
					}
				}
				//reset the all connection downloadingNum to zero
				
				for (int i = 0; i < connectionSize; i++) {
					connectionList.get(i).resetDownloadingNum();
				}

				//write log
				String logStr = "";
				for (int i = 0; i < newPreferedList.size(); i++) {
					Connection connection = newPreferedList.get(i);
					logStr += connection.getOpPeer().getId();
					if (i != newPreferedList.size() - 1) {
						logStr += ",";
					}
				}
				Log.getInstance().writeLog(LogType.ChangeOfPreferredNeighbor, null, logStr);
				//connectionList.get(0).getLogger().writeLog(Type.wri);
				//set the newPreferedList as preferedList
				preferedList = newPreferedList;
			}
			
			//select optimistic neighbor
			if (currentMillis - lastOptimisticUnchokingTime > optimisticUnchokingInterval) {
				List<Connection> interestedButNotInPreferredList = new ArrayList<>();
				lastOptimisticUnchokingTime = currentMillis;
				for (Connection connection : runningConnection) {
					//confirm the connection already finished handShake
					if (connection.getInterestedFlag() && !preferedList.contains(connection)) {
						interestedButNotInPreferredList.add(connection);
					}
				}
				//randomly select one
				if (interestedButNotInPreferredList.size() > 0) {
					Collections.shuffle(interestedButNotInPreferredList);
					optimisticPeer = interestedButNotInPreferredList.get(0);
					//send unchokeMessage
					optimisticPeer.sendMessage(new Message(MessageType.UNCHOKE, null));
					Log.getInstance().writeLog(LogType.ChangeOfOptUnchokedNeighbor, optimisticPeer.getOpPeer(), null);
				}
				
			}
		}
	}
}
