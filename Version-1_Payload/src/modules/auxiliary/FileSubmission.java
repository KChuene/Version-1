package modules.auxiliary;

/**
    Represents the structure of a file submission made by exfiltration module.

    @author Str1k3r
    @version 1.0
*/
public class FileSubmission {

    public String clientName;
    public String name;
    public boolean isBinary = true; // treat as binary by default
    public String content;

    public FileSubmission clientName(String clientName) {
        this.clientName = clientName;

        return this;
    }

    public FileSubmission name(String name) {
        this.name = name;

        return this;
    }

    public FileSubmission isBinary(boolean binary) {
        this.isBinary = binary;

        return this;
    }

    public FileSubmission content(String content) {
        this.content = content;

        return this;
    }
}
