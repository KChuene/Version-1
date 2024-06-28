package modules.auxiliary;

/**
    Represents parameter and it's value for a http request.

    @author Str1k3r
    @version 1.0
*/
public class RequestParameter {
    public String Name;
    public String Value;

    public RequestParameter name(String name) {
        Name = name;

        return this;
    }

    public RequestParameter value(String value) {
        Value = value;

        return this;
    }
}
