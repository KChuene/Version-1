package modules.auxiliary;

import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public class RequestParameterAdapter extends TypeAdapter<RequestParameter> {


    @Override
    public void write(JsonWriter jsonWriter, RequestParameter parameter) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("Name");
        jsonWriter.value(parameter.Name);
        jsonWriter.name("Value");
        jsonWriter.value(parameter.Value);
        jsonWriter.endObject();
    }

    @Override
    public RequestParameter read(JsonReader jsonReader) throws IOException {
        RequestParameter parameter = new RequestParameter();
        jsonReader.beginObject();
        String fieldname = null;

        while (jsonReader.hasNext()) {
            JsonToken token = jsonReader.peek();

            if (token.equals(JsonToken.NAME)) {
                //get the current token
                fieldname = jsonReader.nextName();
            }

            if ("Name".equalsIgnoreCase(fieldname)) {
                //move to next token
                token = jsonReader.peek();
                parameter.Name = jsonReader.nextString();
            }

            if("Value".equalsIgnoreCase(fieldname)) {
                //move to next token
                token = jsonReader.peek();
                parameter.Value = jsonReader.nextString();
            }
        }
        jsonReader.endObject();
        return parameter;
    }
}
