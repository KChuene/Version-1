package modules.auxiliary;

/**
    Represents the result of a command execution to be sent
    back to the API.
 */
public class CmdResult {
    public String targetId;
    public String resultString;

    public CmdResult() {}

    public CmdResult(String targetId, String resultString) {
        this.targetId = targetId;
        this.resultString = resultString;
    }

    public CmdResult targetId(String id) {
        targetId = id;

        return this;
    }

    public CmdResult resultString(String resultString) {
        resultString = resultString;

        return this;
    }
}
