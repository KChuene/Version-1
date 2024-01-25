package modules.auxiliary;

import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

public class FileSubmissionAdapter extends TypeAdapter<FileSubmission> {
    private String clientName;
    private String name;
    private boolean isBinary = true; // treat as binary by default
    private String content;
    @Override
    public void write(JsonWriter jsonWriter, FileSubmission submission) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("clientName");
        jsonWriter.value(submission.clientName);
        jsonWriter.name("name");
        jsonWriter.value(submission.name);
        jsonWriter.name("isBinary");
        jsonWriter.value(submission.isBinary);
        jsonWriter.name("content");
        jsonWriter.value(submission.content);
        jsonWriter.endObject();
    }

    @Override
    public FileSubmission read(JsonReader jsonReader) throws IOException {
        FileSubmission submission = new FileSubmission();
        jsonReader.beginObject();

        String currentField = null;
        while(jsonReader.hasNext()) {
            JsonToken token = jsonReader.peek();

            if(token.equals(JsonToken.NAME)) {
                currentField = jsonReader.nextName();
            }
            else continue;

            switch (currentField.toLowerCase()) {
                case "clientname" -> {
                    jsonReader.peek();
                    submission.clientName = jsonReader.nextString();
                }
                case "name" -> {
                    jsonReader.peek();
                    submission.name = jsonReader.nextString();
                }
                case "isbinary" -> {
                    jsonReader.peek();
                    submission.isBinary = jsonReader.nextBoolean();
                }
                case "content" -> {
                    jsonReader.peek();
                    submission.content = jsonReader.nextString();
                }
            }
        }

        jsonReader.endObject();
        return submission;
    }
}
