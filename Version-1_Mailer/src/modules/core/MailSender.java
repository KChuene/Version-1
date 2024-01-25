package modules.core;

import java.io.File;
import java.util.ArrayList;

import modules.auxiliary.ModuleConfig.MailSenderConfig;
import modules.file.FileHandler;
import modules.net.MailClient;

/**
	Module for sending SE e-mails using MailClient
	
	@author Str1k3r
	@version 1.0
	@see Module, MailClient
 */
public class MailSender implements modules.auxiliary.Module {
	
	private String subject;
	private File messageFile;
	private ArrayList<String> addresses;

	/**
		Create and configure mail sender.
		
		@param config Configuration fields/information.
	*/
	public MailSender(MailSenderConfig config) {
		subject = config.mailSubject;
		addresses = config.targetAddresses;
		messageFile = new File(config.MessageFile);
		
	}
	
	/**
	 	Send email message to set email addresses.
	*/
	public void sendMessage() {
		MailClient mailClient = MailClient.getInstance();
		
		System.out.printf("[*] Loading e-mail message (%s)...\n", messageFile.getName());
		String message = FileHandler.readEmailMessage(messageFile);
		boolean success = mailClient.sendMsg(addresses, subject, message);
		if(success) {
			System.out.println("[*] Emails sent successfully!");
		}
		else {
			System.out.println("[*] Email sending failed.");
		}
	}
}
