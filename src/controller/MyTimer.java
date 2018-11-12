package controller;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import model.Connection;

public class MyTimer extends Thread{
	private List<Connection> connectionList;
	private List<Connection> preferedList;
	private Connection optimisticPeer;
	private int unchokingInterval;
	private int optimisticUnchokingInterval;
	private long lastUnchokingTime;
	private long lastOptimisticUnchokingTime;
	private boolean startFlag;
	
	public MyTimer(List<Connection> connectionList) {
		this.connectionList = connectionList;
		unchokingInterval = ArgReader.getInstance().getUnchokingInterval();
		optimisticUnchokingInterval = ArgReader.getInstance().getOptimisticUnchokingInterval();
		preferedList = new ArrayList<>();
		lastUnchokingTime = 0;
		lastOptimisticUnchokingTime = 0;
		startFlag = true;
	}
	public void startTimer() {
		startFlag = true;
		super.start();
	}
	public void stopTimer() {
		startFlag = false;
	}
	
	@Override
	public void run() {
		while(startFlag) {
			long currentMillis = System.currentTimeMillis();
			//select preferred neighbor
			if (currentMillis - lastUnchokingTime > unchokingInterval) {
				lastUnchokingTime = currentMillis;
				List<Connection> interestedList = new ArrayList<>();
				List<Connection> speedList = new ArrayList<>();
				List<Connection> newPreferedList = new ArrayList<>();
				//get interest list
				for (Connection connection : connectionList) {
					//confirm the connection already finished handShake
					if (connection.getPeerInfo() != null && connection.getInterestedFlag()) {
						interestedList.add(connection);
					}
				}
				//select peer which already have speed in interested list
				for (Connection connection : interestedList) {
					if (connection.getDownloadingNumOfPeriod() > 0) {
						speedList.add(connection);
					}
				}
				//select new prefer list
				if (speedList.size() > 0) {
					if (speedList.size() <= ArgReader.getInstance().getNumberOfPreferredNeighbors()) {
						newPreferedList = speedList;
					} else {
						//sort by downloadingNum
						Collections.sort(speedList, new Comparator<Connection>() {
							@Override
							public int compare(Connection c1, Connection c2) {
								return c1.getDownloadingNumOfPeriod() - c2.getDownloadingNumOfPeriod();
							}
						});
						//add k fast
						for (int i = 0; i < ArgReader.getInstance().getNumberOfPreferredNeighbors(); i++) {
							newPreferedList.add(speedList.get(i));
						}
					}
				}
				for (Connection connection : newPreferedList) {
					//first time choke
					if (preferedList.contains(connection)) {
						//send ChockMessage
					} else {
						//send unchokeMessage
					}
				}
				//reset the downloadingNum to zero
				for (Connection connection : connectionList) {
					connection.resetDownloadingNum();
				}
				//set the newPreferedList to preferedList
				preferedList = newPreferedList;
			}
			
			//select optimistic neighbor
			if (currentMillis - lastOptimisticUnchokingTime > optimisticUnchokingInterval) {
				List<Connection> interestedList = new ArrayList<>();
				List<Connection> interestedButNotInPreferredList = new ArrayList<>();
				
				lastOptimisticUnchokingTime = currentMillis;
				//get interest list
				for (Connection connection : connectionList) {
					//confirm the connection already finished handShake
					if (connection.getPeerInfo() != null && connection.getInterestedFlag()) {
						interestedList.add(connection);
					}
				}
				for (Connection connection : interestedList) {
					if (!preferedList.contains(connection)) {
						interestedButNotInPreferredList.add(connection);
					}
				}
				//randomly select one
				if (interestedButNotInPreferredList.size() > 0) {
					Random rnd = new Random(2);
					Collections.shuffle(interestedButNotInPreferredList, rnd);
					optimisticPeer = interestedButNotInPreferredList.get(0);
					//send unchoke message
				}
				
			}
		}
	}
}
