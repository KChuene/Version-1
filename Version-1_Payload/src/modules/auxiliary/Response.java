package modules.auxiliary;

/**
    Represents the expected (shared) structure of responses between the API and the client.

    @author Str1k3r
    @version 1.0
*/
public class Response {
    public int code;
    public String message;
    public String data;

    public Response() {}
}
