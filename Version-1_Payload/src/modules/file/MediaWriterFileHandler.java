package modules.file;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
    Defines file operations for the MediaWriter module.

    @author Str1k3r
    @version 1.0
 */
public class MediaWriterFileHandler {

    private static final String infectionIdsFile = "./data/infection_ids.dat";

    public static ArrayList<String> readInfectionIdentifiers() {
        File dir = new File("./data/");
        File file = new File(infectionIdsFile);

        try {
            if(!dir.exists()) {
                dir.mkdir();
            }

            if(!file.exists()) {
                file.createNewFile();
            }

            try(ObjectInputStream objInStream = new ObjectInputStream(
                    new FileInputStream(file))) {

                Object read = objInStream.readObject();
                if(read instanceof ArrayList<?>) {
                    return (ArrayList<String>) read;
                }
            }

        }
        catch (Exception ex) {
            // Mainly IO and ClassNotFound exceptions
            /* pass */
        }

        return null;
    }



    public static void writeIdentifierFile(File file) {
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
        }
        catch (Exception ex) { /*pass*/}
    }
}
