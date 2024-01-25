// custom libraries
import modules.auxiliary.*;
import modules.core.*;

/**
    Application entry point class.

    @author Str1k3r
    @version 1.0
    @see Options, Copy
*/
public class Main {

    /**
        Main method.

        @param args Command line arguments. Program options.
    */
    public static void main(String[] args) {
 
        // Create appropriate module
        modules.auxiliary.Module module = Options.evaluate(args);
        if(module == null) {
        	Manager.exit(ExecCode.INVALID_MODULE_SPECIFIED);
        }
        
        if(module instanceof MailSender) {
        	((MailSender) module).sendMessage();
        }


        System.out.println("----------------- END ----------------");

    }


}