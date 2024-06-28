package modules.auxiliary;

import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class CmdResultAdapter extends TypeAdapter<CmdResult> {

    @Override
    public void write(JsonWriter jsonWriter, CmdResult cmdResult) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("targetId");
        jsonWriter.value(cmdResult.targetId);
        jsonWriter.name("resultString");
        jsonWriter.value(cmdResult.resultString);
        jsonWriter.endObject();
    }

    @Override
    public CmdResult read(JsonReader jsonReader) throws IOException {
        CmdResult cmdResult = new CmdResult();
        jsonReader.beginObject();

        String fieldname = null;
        while(jsonReader.hasNext()) {
            JsonToken token = jsonReader.peek();

            if(token.equals(JsonToken.NAME)) {
                fieldname = jsonReader.nextName();
            }
            else continue;

            switch (fieldname.toLowerCase()) {
                case "targetid" -> {
                    jsonReader.peek();
                    cmdResult.targetId = jsonReader.nextString();
                }
                case "resultstring" -> {
                    jsonReader.peek();
                    cmdResult.resultString = jsonReader.nextString();
                }
            }
        }

        jsonReader.endObject();
        return cmdResult;
    }
}
