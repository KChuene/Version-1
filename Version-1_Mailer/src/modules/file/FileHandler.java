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
public class FileHandler {
	
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
			
			if(lines.isEmpty()) {
				Manager.exit(ExecCode.EMPTY_MAIL_ADDRESSES_FILE); // none read
			}

		}
		catch(FileNotFoundException fileNotFoundEx) {
			Manager.exit(ExecCode.MAIL_ADDRESSES_FILE_NOTFOUND);
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
				Manager.exit(ExecCode.EMPTY_MAIL_MESSAGE_FILE);
			}
		}
		catch(FileNotFoundException fileNotFoundEx) {
			Manager.exit(ExecCode.MAIL_MESSAGE_FILE_NOTFOUND);
		}
		
		return message.toString();
	}

}
