package controller;

import java.io.*;
import controller.ArgReader;

public class FileManager {
	private static FileManager instance;
	private String filePath;
	private File file;
	private ArgReader argReader;
	private FileManager() {
		
	}
	public static FileManager getInstance() {
		if (instance == null) {
			instance = new FileManager();
		}
		return instance;
	}
	public void setFile(int id, ArgReader argReader, boolean haveFile) {
		this.filePath = "peer" + id + "/" + argReader.getfileName();
		this.file = new File(filePath);
		if(haveFile) {				//When we are supposed to have the file, we do
			if(!file.exists()) {
				System.out.println("Error: File doesn't exist.");
				System.exit(0);
			}
		}
		else {						//When we are not supposed to have the file, we do
			File dir = new File("peer" + id);
			dir.mkdir();
			file.delete();			//Delete and start writing to .temp file to keep intention clean
			file = new File(filePath + ".temp");
			file.delete();
		}
		this.argReader = argReader;
	}
	
	public byte[] read(int piece) {
		try {
			RandomAccessFile input = new RandomAccessFile(file, "r");		//Open RandomAccessFile to read at desired position
			input.seek(piece * argReader.getpieceSize());					//Seek to desired position
			int piecesNum = argReader.getfileSize() / argReader.getpieceSize();		//The number of pieces
			if(argReader.getfileSize() % argReader.getpieceSize() != 0) {			//Last piece is not as big as before
				piecesNum ++;									
			}
			byte[] pieceBuffer;
			if(piece == (piecesNum - 1)) {
				pieceBuffer = new byte[argReader.getfileSize() - (piecesNum - 1) * argReader.getpieceSize()];	//Create a buffer of piece size and read data
			}
			else {
				pieceBuffer = new byte[argReader.getpieceSize()];
			}
			input.read(pieceBuffer);
			input.close();
			return pieceBuffer;
		} catch (Exception e) {
			System.out.println("Error: could not read piece " + piece);
			e.printStackTrace();
			return null;
		}
	}
	
	public void write(int piece, byte[] data) {
		try {
			RandomAccessFile output = new RandomAccessFile(file, "rw");			//Open RandomAccessFile to write at desired position
			output.seek(piece * argReader.getpieceSize());						//Seek to desired position
			output.write(data);
			output.close();
		} catch (Exception e) {
			System.out.println("Error: could not write piece " + piece);
		}
	}
	
	public void finalize() {
		File completedFile = new File(filePath);				//Since we've been writing to <filePath>.temp rename to <filePath>
		file.renameTo(completedFile);
		file = completedFile;
	}
}
