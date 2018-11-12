package controller;
import java.io.*;
import java.util.*;
import model.*;

public class BitfieldManager {
	private static BitfieldManager instance;
	
	private int peerNum = PeerInfoManager.getInstance().getSize();
	private int pieceSize = ArgReader.getInstance().getfileSize()/ArgReader.getInstance().getfileSize();
	private boolean[] ownBitfield = new boolean[pieceSize];
	//private boolean[][] othersBitfield = new boolean[peerNum+1][pieceSize+1] ;
	HashMap<Integer, boolean[]> othersBitfield = new HashMap<>();
	public static BitfieldManager getInstance(){
		if (instance == null) {
			instance = new BitfieldManager();
		}
		return instance;
	}
	/*
	public boolean[] getmyBitfield(){
		return ownBitfield;
	}
	
	public boolean[] getOthersBitfield(int peerID){
		//return othersBitfield[peerID];
		return null;
	}
	*/
	public void updateMyBitfield(int pieceNum){
		ownBitfield[pieceNum] = true;
	}
	//for receive have message
	public void updateOtherBitfield(int peerID, int pieceNum){
		//othersBitfield[peerID][pieceNum] = true;
	}
	
	public int compareAndchoose(int otherID){
		return 1;
	}
}

