package controller;

import java.io.*;
import controller.ArgReader;

public class FileManager {
	private static FileManager instance;
	private String filePath;
	private File file;
	private FileManager() {
		
	}
	public static FileManager getInstance() {
		if (instance == null) {
			instance = new FileManager();
		}
		return instance;
	}
	public void setFile(int id, boolean haveFile) {
		this.filePath = "./src/" + ArgReader.getInstance().getfileName();		//file address
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
		
	}
	
	public byte[] read(int piece) {
		try {
			RandomAccessFile input = new RandomAccessFile(file, "r");		//Open RandomAccessFile to read at desired position
			input.seek(piece * ArgReader.getInstance().getpieceSize());					//Seek to desired position
			int piecesNum = ArgReader.getInstance().getfileSize() / ArgReader.getInstance().getpieceSize();		//The number of pieces
			if(ArgReader.getInstance().getfileSize() % ArgReader.getInstance().getpieceSize() != 0) {			//Last piece is not as big as before
				piecesNum ++;									
			}
			byte[] pieceBuffer;
			if(piece == (piecesNum - 1)) {
				pieceBuffer = new byte[ArgReader.getInstance().getfileSize() - (piecesNum - 1) * ArgReader.getInstance().getpieceSize()];	//Create a buffer of piece size and read data
				System.out.println("read" + pieceBuffer.length);
			}
			else {
				pieceBuffer = new byte[ArgReader.getInstance().getpieceSize()];
				System.out.println("read" + pieceBuffer.length);
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
			output.seek(piece * ArgReader.getInstance().getpieceSize());						//Seek to desired position
			System.out.println("write" + data.length);
			output.write(data);
			output.close();
		} catch (Exception e) {
			System.out.println("Error: could not write piece " + piece);
		}
	}
	
	public void finalize() {
		String filePath1 = "./src/" + ArgReader.getInstance().getfileName() + "1";
		File completedFile = new File(filePath1);				//Since we've been writing to <filePath>.temp rename to <filePath>
		file.renameTo(completedFile);
		file = completedFile;
	}
}
