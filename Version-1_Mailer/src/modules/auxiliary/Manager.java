package modules.auxiliary;

import java.io.File;
import java.net.http.HttpResponse;
import java.util.LinkedList;

// custom libraries
import modules.net.*;
import modules.file.*;

/**
    Helper class for all program modules, provides helper functionality.

    @author Str1k3r
    @version 1.0
*/
public class Manager {

	public static final int DEFAULT_NOTHREADS = 5; // default no. of threads to execute modules over
    public static final String PAYLOAD_DIR = "./data/";
    private final int MAX_SUBDIRS = 500;
    
    /**
    	Terminates the program with execution status.

    	@param execCode The code representing the execution status.
	*/
	public static void exit(ExecCode execCode) {
	    // output exec msg and terminate
	    System.out.println(execCode.message());
	    System.exit(execCode.code());
	}
}