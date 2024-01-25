package modules.auxiliary;

import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public class ResponseAdapter extends TypeAdapter<Response> {


    @Override
    public void write(JsonWriter jsonWriter, Response response) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("code");
        jsonWriter.value(response.code);
        jsonWriter.name("message");
        jsonWriter.value(response.message);
        jsonWriter.name("data");
        jsonWriter.value(response.data);
        jsonWriter.endObject();
    }

    @Override
    public Response read(JsonReader jsonReader) throws IOException {
        Response response = new Response();
        jsonReader.beginObject();

        String fieldname = null;
        while(jsonReader.hasNext()) {
            JsonToken token = jsonReader.peek();

            if (token.equals(JsonToken.NAME)) {
                //get the current token
                fieldname = jsonReader.nextName();
            }

            switch (fieldname.toLowerCase()) {
                case "code" -> {
                    jsonReader.peek();
                    response.code = jsonReader.nextInt();
                }
                case "message" -> {
                    jsonReader.peek();
                    response.message = jsonReader.nextString();
                }
                case "data" -> {
                    jsonReader.peek();
                    response.data = jsonReader.nextString();
                }
            }
        }

        jsonReader.endObject();
        return response;
    }
}
