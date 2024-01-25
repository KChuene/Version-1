package modules.net;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import modules.auxiliary.ExecCode;
import modules.auxiliary.MailCreds;
import modules.auxiliary.Manager;

/**
 	Singleton E-mail client. Primary purpose of sending Social Engineering e-mails.
 	
 	@author Str1k3r
 	@version 1.0
 */
public class MailClient {
	
	private final String smtpSvr = "smtp.ethereal.email"; // "smtp.mail.yahoo.com";
	//private final String smtpSvr = "smtp.mail.yahoo.com";
	private final String smtpSvrPort = "587";
	private final String uname = MailCreds.Email; // "marisa.ohara40@ethereal.email"; // "version1mail@yahoo.com";
	//private final String uname = "version1mail@yahoo.com";
	private final String upass = MailCreds.Password; // "V3r$!on1MAILY@hooSv3r";
	//private final String upass = "V3r$!on1MAILY@hooSv3r";
	private final Session session;
	private static MailClient instance = null;

	
	/**
	 	Obtain Session instance and set session Properties.
	 */
	
	private MailClient() {
		/*
		1. Set session properties - server, auth requirement
		2. Obtain session - setting properties, and authenticating
		*/
		
		// Remote server setup
		// Reference: https://stackoverflow.com/questions/38608089/could-not-connect-to-smtp-host-smtp-gmail-com-port-587-nested-exception-is
		Properties properties = new Properties();
		properties.put("mail.smtp.host", smtpSvr); // server address
		properties.put("mail.smtp.auth", true); // authentication (with username and password) required by server
		properties.put("mail.smtp.port", smtpSvrPort); // server port
		properties.put("mail.smtp.starttls.enable", "true"); // tell server we want to communicate of secure channel, required by server
		properties.put("mail.smtp.ssl.trust", smtpSvr); // IMPORTANT: Trust the mail server SSL certificate, otherwise results in MessagingExceotion

		session = Session.getInstance(properties, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(uname, upass);
			}
		});
	}
	
	/**
	 	Create and/or return MailClient instance.
	
		@return Returns current MailClient instance.
	*/
	public static MailClient getInstance() {
		if(instance == null) {
			instance = new MailClient();
		}
		
		return instance;
	}
	
	/** 
		Compose email message (Mime Message) with necessary header fields.
		
		@param from Sender of the message.
		@param subject Topic of the message.
		@param body The message itself. (discussion)
		@return Returns a constructed MimeMessage from provided parameters (fields).
	 */
	private MimeMessage composeMsg(String from, String subject, String body) {
		// Necessary fields:
		// from, subject, body
		
		MimeMessage message = new MimeMessage(session);
		
		try {
			message.setFrom(new InternetAddress(uname));
			message.setSubject(subject);
			message.setContent(body, "text/html");
			
			return message;
		}
		catch(Exception ex) {
			System.out.println(ex.getMessage());
			return null;
		}
		
	}
	
	/**
		Send email to one or more recipients.

		@param recipients List of addresses to receive the email.
		@param subject Topic of the email.
		@param body The message.
		@return Returns true, if at least one message was sent, false otherwise.
	 */
	public boolean sendMsg(ArrayList<String> recipients, String subject, String body) {
		
		MimeMessage message = composeMsg(uname, subject, body);
		if(message == null) {
			// failed to compose message
			Manager.exit(ExecCode.COMPOSE_MSG_FAIL);
		}
		System.out.printf("[*] Sending (%d addresse(s)): \n", recipients.size());

		// Send email message to each recipient separately
		int successfulSends = recipients.size(); // assume all emails will be sent successfully
		for(String to : recipients) {
			System.out.printf("=> %s\t\t", to);
			try {
				message.setRecipient(RecipientType.TO, new InternetAddress(to));
				Transport.send(message);

				System.out.println("<SUCCESS>");
			}
			catch(MessagingException msgEx) {
				System.out.println(msgEx.getMessage());
				successfulSends--; // continue on to the next recipient

			}
			catch (Exception ex) {
				System.out.println(ex.getMessage());
				successfulSends--; // continue on to the next recipient
			}
		}

		if(!(successfulSends > 0)) {
			// none succeeded - assume bigger issue
			return false;
		}
		
		System.out.printf("Sent %d e-mail(s).\n", successfulSends);
		return true; // even if some failed
	}

}
