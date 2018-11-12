package controller;
import java.io.*;
import java.util.*;
import model.*;

public class BitfieldManager {
	private static BitfieldManager instance;
	private int pieceSize = ArgReader.getInstance().getfileSize()/ArgReader.getInstance().getfileSize();
	HashMap<PeerInfo, boolean[]> bitFields = new HashMap<>();
	private BitfieldManager(){
		//PeerInfoManagerPeer
		for (PeerInfo peer : PeerInfoManager.getInstance().getPeers()){
			boolean singlePeer[] = new boolean[pieceSize];
			if(peer.getHasFile()){
				Arrays.fill(singlePeer, true);
				bitFields.put(peer, singlePeer);
			}
			else{
				Arrays.fill(singlePeer, false);
				bitFields.put(peer, singlePeer);
			}
		}
	}
	public static BitfieldManager getInstance(){
		if (instance == null) {
			instance = new BitfieldManager();
		}
		return instance;
	}

	//for receive have message
	synchronized public void updateBitfield(int peerInfo, int pieceNum){
		//othersBitfield[peerID][pieceNum] = true;
		boolean[] tmp = bitFields.get(peerInfo);
		tmp[pieceNum] = true;
	}
	
	public int compareAndchoose(PeerInfo peerInfo){
		//compare if there is interested pieces
		boolean[] myBitField = bitFields.get(PeerInfoManager.getInstance().getMyInfo());
		boolean[] destBitField = bitFields.get(peerInfo);
		int[] diff = new int[pieceSize];
		boolean flag = false;
		Arrays.fill(diff, 0);
		for (int i = 0; i < pieceSize; i++){
			if(myBitField[i] == false && destBitField[i] == true){
				diff[i] = i;
				flag = true;
			}
		}
		//no interested pieces
		if (flag == false){
			return -1;
		}
		else {
			//random choose a file piece
			int want = new Random().nextInt(diff.length);
			return want;
		}
		
	}
}

