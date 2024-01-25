package modules.auxiliary;

import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public class CommandAdapter extends TypeAdapter<Command> {

    @Override
    public void write(JsonWriter jsonWriter, Command command) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("issuerId");
        jsonWriter.value(command.issuerId);
        jsonWriter.name("targetId");
        jsonWriter.value(command.targetId);
        jsonWriter.name("isShellCmd");
        jsonWriter.value(command.isShellCmd);
        jsonWriter.name("cmdString");
        jsonWriter.value(command.cmdString);
        jsonWriter.endObject();
    }

    @Override
    public Command read(JsonReader jsonReader) throws IOException {
        Command command = new Command();
        jsonReader.beginObject();
        String fieldname = null;

        while (jsonReader.hasNext()) {
            JsonToken token = jsonReader.peek();

            if (token.equals(JsonToken.NAME)) {
                //get the current token
                fieldname = jsonReader.nextName();
            }
            else continue;

            switch(fieldname.toLowerCase()) {
                case "issuerid" -> {
                    jsonReader.peek();
                    command.issuerId = jsonReader.nextString();
                }
                case "targetid" -> {
                    jsonReader.peek();
                    command.targetId = jsonReader.nextString();
                }
                case "isshellcmd" -> {
                    jsonReader.peek();
                    command.isShellCmd = jsonReader.nextBoolean();
                }
                case "cmdstring" -> {
                    jsonReader.peek();
                    command.cmdString = jsonReader.nextString();
                }
            }

        }

        jsonReader.endObject();
        return command;
    }
}
