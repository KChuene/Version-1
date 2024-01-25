package modules.auxiliary;

import java.net.URI;
import java.util.ArrayList;

/**
	A struct like class for setting up configuration of modules
*/
public class ModuleConfig { //TODO: Find a better way to represent configuration state
	public int threadCount = Manager.DEFAULT_NOTHREADS;
	
	public static class CopyConfig extends ModuleConfig {
		public String startPath = null;
		public boolean recursive = false;
		public String payloadURL = null;
		public int copyLimit = -1;

		public CopyConfig startPath(String path) {
			startPath = path;
			return this;
		}

		public CopyConfig isRecursive(boolean isRecursive) {
			recursive = isRecursive;
			return this;
		}

		public CopyConfig payloadURL(String url) {
			payloadURL = url;
			return this;
		}

		public CopyConfig copyLimit(int limit) {
			copyLimit = limit;
			return this;
		}
	}
	
	public static class MailSenderConfig extends ModuleConfig {
		public String mailSubject = null;
		public String targetAddress = null;
		public ArrayList<String> targetAddresses = null;
		public String MessageFile = null;


	}
}
