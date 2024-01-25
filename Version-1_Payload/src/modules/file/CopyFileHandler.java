package modules.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

// custom libraries
import modules.auxiliary.*;

/**
 	Module for reading and writing files.
 	
 	@author Str1k3r
 	@version 1.0
*/
public class CopyFileHandler implements Runnable {
	
	private BufferedInputStream inStream;
	private BufferedOutputStream outStream;
	private int bufferSize;
	
	/**
	 	Create new file handler over specified input and output streams with specific buffer size.
	 	
	 	@param inStream Input stream to read over.
	 	@param outStream Output stream to write over.
	 	@param bufferSize Underlying buffer size.
	*/
	public CopyFileHandler(BufferedInputStream inStream, BufferedOutputStream outStream, int bufferSize) {
		this.inStream = inStream;
		this.outStream = outStream;
		this.bufferSize = bufferSize;
	}
	
	/**
	 	Overrides Runnable.run() for threaded execution.
	*/
	@Override
	public void run() {
		duplicate(); // delegate
	}
	
	/**
	 	Carry out file read and write over instance streams, from one to the other.
	*/
	private void duplicate() {
		// Read from one stream then Write to the other until complete 
		try {
			int available = 0;
			
			// read bufferSize or available bytes from the one stream and write to the other 
			while((available = inStream.available()) > 0) {
				final int readSize = (bufferSize <= available)? bufferSize:available;
				
				byte[] bytesRead = inStream.readNBytes(readSize);
				outStream.write(bytesRead, 0, readSize);
			}
		}
		catch(IOException ioEx) {
			System.out.println("Error reading/writing current duplication.");
		}
		catch(OutOfMemoryError memErr) {
			Manager.exit(EExecCode.INSUFFICIENT_MEMORY);
		}
		finally {
			closeStreams(inStream, outStream);
		}
	}
	
	/**
	 	Save an array of bytes to specified path.
	 	
	 	@param destDir The path on storage to save to.
	 	@param data The byte array to save.
	 	@return Returns true if completed successfully, false otherwise.
	*/
	public static boolean saveBytes(String destDir, String destFilename, byte[] data) {
		// first create the directory if not exist
		File destDirFile = new File(destDir);
		try {
			destDirFile.mkdir();
		}
		catch(SecurityException secEx) { return false; } // Access denied

 
		// save using automatic resource management
		String destPath = String.format("%s%s", destDir, destFilename);
		try(BufferedOutputStream outStream = new BufferedOutputStream(
				new FileOutputStream(destPath))) {
			
			outStream.write(data, 0, data.length);
			return true;
		}
		catch(IOException ex) {
			return false;
		}
		
	}
	
	/**
		Read in email addresses from a file line by line.
		
		@param filehandle The file to read from.
		@return Returns a list (ArrayList) of all email addresses read.
	*/
	public static ArrayList<String> readEmailAddresses(File filehandle) {
		ArrayList<String> lines = new ArrayList<String>();
		try(Scanner reader = new Scanner(filehandle)) {
			// read in all email addresses - expected to be separated line by line
			while(reader.hasNextLine()) {
				lines.add(reader.nextLine());
			}
			
			if(!(lines.size() > 0)) {
				Manager.exit(EExecCode.EMPTY_MAIL_ADDRESSES_FILE); // none read
			}

		}
		catch(FileNotFoundException fileNotFoundEx) {
			Manager.exit(EExecCode.MAIL_ADDRESSES_FILE_NOTFOUND);
		}
		
		return lines;
	}
	
	/**
	 	Read and return the entire email message file.
	 	
	 	@param filehandle to read from the file.
	 	@return A string of the contents of the entire file.
	*/
	public static String readEmailMessage(File filehandle) {
		StringBuilder message = new StringBuilder("");
		try(Scanner reader = new Scanner(filehandle)) {
			while(reader.hasNextLine()) {
				message.append(reader.nextLine()); 
			}
			
			if(message.toString().equals("")) {
				Manager.exit(EExecCode.EMPTY_MAIL_MESSAGE_FILE);
			}
		}
		catch(FileNotFoundException fileNotFoundEx) {
			Manager.exit(EExecCode.MAIL_MESSAGE_FILE_NOTFOUND);
		}
		
		return message.toString();
	}
	
    /** 
	    Close provided input and output streams.
	
	    @param inStream Input stream to close.
	    @param outStream Output stream to close.
	*/
	private void closeStreams(InputStream inStream, OutputStream outStream) {
	    try {
	        if(inStream != null) {
	            inStream.close();
	        }
	
	        if(outStream != null) {
	            outStream.close();
	        }
	    }
	    catch(IOException ioEx) {
	        Manager.exit(EExecCode.STREAM_CLOSE_FAIL);
	    }
	    finally {
	        inStream = null; // even if stream is null, set null for incase it wasn't and close failed
	        outStream = null;
	    }
	}
}
