package controller;
import java.io.*;
import java.util.*;
import model.*;

public class PeerInfoController {
	private static PeerInfoController instance;
	private PeerInfo myInfo;
	private List<PeerInfo> peerInfoList;
	private PeerInfoController() {
		try {
			FileReader reader = new FileReader("./PeerInfo.cfg");
            BufferedReader bufferedReader = new BufferedReader(reader);
            peerInfoList = new ArrayList<>();
            String str = null;
            while((str = bufferedReader.readLine()) != null) {
            	String[] peerArgs = str.split(" ");
            	PeerInfo info = new PeerInfo(Integer.parseInt(peerArgs[0]), 
            			peerArgs[1], Integer.parseInt(peerArgs[2]), peerArgs[3] == "1" ? true : false);
            	peerInfoList.add(info);
            }
            
            bufferedReader.close();
            reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static PeerInfoController getInstance() {
		if (instance == null) {
			instance = new PeerInfoController();
		}
		return instance;
	}
	public PeerInfo getMyInfo() {
		return myInfo;
	}
	public void setMyInfo(PeerInfo info) {
		myInfo = info;
	}
	
	public List<PeerInfo> getPeers() {
		return peerInfoList;
	}
	
	public PeerInfo getPeerInfoById(int id) throws Exception{
		PeerInfo ans = null;
		for (PeerInfo peer : peerInfoList) {
			if (peer.getId() == id) {
				ans = peer;
			}
		}
		if (ans == null) {
			throw new Exception("could not get peerinfo");
		}
		return ans;
	}
	
	public List<PeerInfo> getPeersBefore(PeerInfo info) {
		List<PeerInfo> ans = new ArrayList<>();
		for (PeerInfo peer : peerInfoList) {
			if (peer.getId() == info.getId()) {
				break;
			} else {
				ans.add(peer);
			}
		}
		return ans;
	}
	public List<PeerInfo> getPeersAfter(PeerInfo info) {
		List<PeerInfo> ans = new ArrayList<>();
		int flag = 0;
		for (PeerInfo peer : peerInfoList) {
			if (peer.getId() == info.getId()) {
				flag = 1;
				continue;
			}
			if (flag == 1) {
				ans.add(peer);
			}
		}
		return ans;
	}
	
	public int getSize() {
		return peerInfoList.size();
	}
	

}
