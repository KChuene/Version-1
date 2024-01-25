package modules.auxiliary;

/**
    Enuemration of exec codes and their description.

    @author Str1k3r
    @version 1.0
*/
public enum ExecCode {
    NO_ARGS_PROVIDED(1, "No arguments provided."),
    PATH_NOT_EXIST(2, "Path not found."),
    NOT_DIRECTORY(3, "Path not directory."),
    CANNOT_READ_DIR(4, "Directory access denied."),
    STREAM_SETUP_FAIL(5, "Failed to setup piped streams."),
    STREAM_CLOSE_FAIL(6, "Failed to close one or more file streams."),
	FETCH_PAYLOAD_FAIL(7, "Failed to download and save payload."),
	INSUFFICIENT_MEMORY(8, "Insufficient memory to continue read/write process."),
	FETCH_LISTING_FAIL(9, "Error fetching start path directory listing. Path possibly not directory."),
	START_PATH_ACCESS_DENIED(10, "Access denied. Cannot access start path."),
	UNRECOGNOZED_MODULE_NAME(11, "Provided module name unrecognized."),
	INSUFFICIENT_MODULE_ARGS(12, "Insufficient arguments for specified module."),
	MULTIPLE_MODULES_SPECIFIED(13, "One module expected, but multiple specified."),
	ARGVALUE_NOT_PROVIDED(14, "Argument value expected, but none found."),
	INVALID_ARGVALUE(15, "An invalid argument value was provided."),
	MANDATORY_ARGS_MISSING(16, "Not all necessary (<>) arguments were provided."),
	CREATE_HTTPCLIENT_FAIL(17, "Failed to create http client. Cannot allocate required IO resources."),
	EMPTY_GET_URI(18, "Empty/null URI provided for request."),
	GET_URI_PARSE_FAIL(19, "Failed to parse provided request URI."),
	REQUEST_DENIED(20, "Failed to make request. Firewall may be blocking connection attempts."),
	REQUEST_INTERRUPT(21, "Request interrupted."),
	REQUEST_IO_FAIL(22, "IO failure sending/receiving the request/response."),
	INVALID_PAYLOAD_URI(23, "Provided payload URL invalid."),
    UNEXPECTED_TERMINATION(24, "Process unexpectedly terminated."),
    COMPOSE_MSG_FAIL(25, "Failed to construct e-mail message."),
    EMPTY_MAIL_ADDRESSES_FILE(26, "No addresses found or read from file."),
    MAIL_ADDRESSES_FILE_NOTFOUND(27, "Email addresses file not found."),
    MAIL_MESSAGE_FILE_NOTFOUND(28, "Email message file not found."),
    EMPTY_MAIL_MESSAGE_FILE(29, "Provided email message file is empty."),
    INVALID_MODULE_SPECIFIED(30, "Invalid module specified.");
	
	


    private final int code; // static-optimisation
    private final String message; 

    private ExecCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
        @return Returns the exec code numeric value.
    */
    public int code() { return code; }

    /**
        @return Returns the exec code description.
    */
    public String message() { return message; }
}