package modules.auxiliary;

import java.util.ArrayList;

/**
	A struct like class for setting up configuration of modules
*/
public class ModuleConfig { //TODO: Find a better way to represent configuration state
	
	public static class MailSenderConfig extends ModuleConfig {
		public String mailSubject = null;
		public String targetAddress = null;
		public ArrayList<String> targetAddresses = null;
		public String MessageFile = null;
	}
}
