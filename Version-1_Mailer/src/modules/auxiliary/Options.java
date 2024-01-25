package modules.auxiliary;

import java.io.File;
import java.util.ArrayList;

// custom libraries
import modules.core.*;
import modules.file.FileHandler;

/**
Class for evaluating command line arguments and creates appropriate Modules using a 
factory-design-like pattern.
 	.
 	@author Str1k3r
 	@version 1.0
*/
public class Options {
	
	private static final String[] modules = {"mailsender"};
	private static final String[] moduleOpts = {"-t", "-r", "-url", "-sp", "-limit"};
	
	/**
		Evaluate command line arguments to determine program execution module.
		
		@param args The string sequence arguments to evaluate to a module.
		
		@return
			Returns a Module instance.
	*/
	public static final Module evaluate(String[] args) {
		valArgs(args); // ensure correct module and number of options
		
		// Create appropriate module
		switch(args[0].toLowerCase()) {

		case "mailsender":{
			return createMailSenderModule(args); // mailsender module
		}
		}
		
		return null;
	}
	
	private static Module createCryptModule(String[] args) { return null; }
	
	/**
		Create a MailSender module with configuration specified by args.
		
		@param args Arguments to configure the MailSender module.
		@return Returns a MailSender module instance.
	*/
	private static Module createMailSenderModule(String[] args) {
		// Create configuration object, set configuration fields as commandline arguments ar
		// evaluated.
		ModuleConfig.MailSenderConfig config = new ModuleConfig.MailSenderConfig(); 
		for(int index = 1; index < args.length; index++) {
			
			// module name should not be specified as an option
			if(in(args[index], modules)) {
				usage();
				Manager.exit(ExecCode.MULTIPLE_MODULES_SPECIFIED);
			}
			
			// set configuration field from arg value
			switch(args[index]) {
			case "-address": {
				valArgValue(args, index); // of arg at index
				
				config.targetAddress = args[index + 1];
				break;
			}
			case "-subject": {
				valArgValue(args, index);
				
				config.mailSubject = args[index + 1];
				break;
			}
			case "-messagefile": {
				valArgValue(args, index);
				
				config.MessageFile = args[index + 1];
				break;
			}
			case "-addressfile": {
				valArgValue(args, index);
				
				// we read the file and set the config field
				ArrayList<String> addresses = FileHandler.readEmailAddresses(
						new File(args[index + 1]));
				
				config.targetAddresses = addresses;
				break;
			}
			}// END SWITCH
		
		}// END FOR
		
		// primary args must be set (note: either -address or -addressfile MUST be specified)
		if((config.targetAddress == null && config.targetAddresses == null) ||
				config.MessageFile == null || config.mailSubject == null) {
			usage();
			Manager.exit(ExecCode.MANDATORY_ARGS_MISSING);
		}
		
		// MailClient will expect a list of addresses
		if(config.targetAddresses == null) {
			config.targetAddresses = new ArrayList<String>(); // create that list anyway
		}
		
		// just combine main address ("-address") into list of addresses if specified
		if(config.targetAddress != null) {
			config.targetAddresses.add(config.targetAddress);
		}

		return new MailSender(config);
	}
	
	/**
		Validate provided command line arguments. Valid module needs to be specified 
		with the correct number of options.
		
		@param args The string sequence of command line arguments to validate.
	*/
	private static void valArgs(String[] args) {
		//TODO: Uses a regex pattern for base check
		System.out.printf("Arguments provided (%d):%n", args.length);
		
		if(args==null || (args.length < 1)) {
			usage();
			Manager.exit(ExecCode.NO_ARGS_PROVIDED);
		}
		
		switch(args[0]) {
		case "mailsender": {
			// expect n number of module args at minimum
			if(!(args.length - 1 >= 6)) {
				usage();
				Manager.exit(ExecCode.INSUFFICIENT_MODULE_ARGS);			
			}
			break;
		}
		case "-h": {
			usage();
			System.exit(0);
		}
		default: {
			usage();
			Manager.exit(ExecCode.UNRECOGNOZED_MODULE_NAME);
		}
		}
	}
	
	/**
		Validate value of argument at provided index. Must exist and not be an option.
		
		@param args The list of arguments.
		@param index The index of the argument to validate.
	*/
	private static void valArgValue(String[] args, int index) {
		// make sure option/arg value is provided and is not another option
		if(!(args.length-1 > index)  || in(args[index + 1], moduleOpts)) {
			usage();
			Manager.exit(ExecCode.ARGVALUE_NOT_PROVIDED);
		}
	}
	
	/**
		Displays program usage; modules and expected options.
	*/
	private static void usage() {

		print("usage: program.jar mailsender -address <mail_address> -subject <mail_subject> -messagefile <msg_file>");
		print("usage: program.jar mailsender -addressfile <mail_address_file> -subject <mail_subject> -messagefile <msg_file>");
		print("");
		print("Modules:");

		print("mailsender \tExecute mail sender module. Send an email message as specified by a file ");
		print("to an email address or addresses specified by a file.");
		print("");
		print("Module Options:");
		print("-address <mail_address> \tThe email address to send to.");
		print("-subject <mail_subject> \tSubject of the email.");
		print("-messagefile <msg_file> \tThe file with the e-mail message. Can be plaintext or HTML format.");
		print("-addressfile <mail_address_file> \tA file of line separated e-mail addresses to send the message to.");
		print("");
	}
	
	/**
	 	A shorthand method for System.out.println() function.
	 	@param text Text to output
	 */
	private static void print(String text) { System.out.println(text); }
	
	/**
		Iterates a collection to search for a specified value.
		
		@param option value to lookup
		@param querySet Set of options to look through for value
		@return Returns true if option found in querySet, false otherwise.
	*/
	private static boolean in(String option, String... querySet) {
		for(String item : querySet) {
			if(option.equals(item)) {
				return true;
			}
		}
		
		return false;
	}
}
