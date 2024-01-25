package modules.net;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.regex.Pattern;

// custom libraries
import modules.auxiliary.*;

/**
 	A singleton that provides the functionality to make http requests
 	
 	@author Str1k3r
 	@version 1.0
*/
public class CopyHttpClient {
	
	private HttpClient client = null;
	private static CopyHttpClient instance = null;
	private final Pattern URLExpression = Pattern.compile("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"); // lazy URL check regex: protocol-(domain-type)-path
	
	/**
	  Create a new HTTP client.
	*/
	private CopyHttpClient() {
		client = Manager.getInstance().createHttpClient();
	}
	
	/**
	 	Creates and/or returns an instance to the HTTPClient class.
	 	
	 	@return Returns a HTTPClient instance
	*/
	public static CopyHttpClient getInstance() {
		if(instance == null) {
			instance = new CopyHttpClient();
		}
		
		return instance;
	}
	
	/**
	 	Make a HTTP GET request to the provided URI string.
	 	
	 	@param uriString The URI to send a request to.
	 	@return Returns a HTTP Response object to the request, otherwise null.
	*/
	public HttpResponse<String> get(String uriString) {
		if(uriString == null) {
			Manager.exit(EExecCode.EMPTY_GET_URI);
		}

		if(!URLExpression.matcher(uriString).matches()) {
			Manager.exit(EExecCode.INVALID_PAYLOAD_URI);
		}
		
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(uriString))
					.version(HttpClient.Version.HTTP_1_1)
					.GET()
					.build();
			
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			if(response.statusCode() != 200) {
				// not found / invalid request / etc
				return null;
			}
			return response;
		}
		catch(IllegalArgumentException illegalArgEx) {
			Manager.exit(EExecCode.INVALID_PAYLOAD_URI);
		}
		catch(IOException ioEx) {
			Manager.exit(EExecCode.REQUEST_IO_FAIL);
		}
		catch(InterruptedException interruptEx) {
			Manager.exit(EExecCode.REQUEST_INTERRUPT);
		}
		catch(SecurityException secEx) {
			Manager.exit(EExecCode.REQUEST_DENIED);
		}
		return null;
	}
}
