/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Utility class for sending emails.  Requires the mail.jar and activation.jar files.
 * Note that the activation.jar is not required if using the Java JRE 6.
 *
 * Emails are sent using SMTP.
 *
 * @author Chris
 * @date 30-Nov-07
 */
public class EmailUtil {

	public static boolean DEBUG = false;

	private static final String FROM = "chisel-support@cs.uvic.ca";
	private static final String HOST = "mail.cs.uvic.ca";

	public static void sendChiselEmail(String to, String msgText, String subject) throws Exception {
		sendEmail(to, FROM, HOST, msgText, subject);
	}

	public static void sendEmail(String to, String from, String host, String msgText,
			String subject) throws Exception {
		sendAttachmentEmail(to, from, host, null, msgText, subject);
	}

	public static void sendChiselAttachmentEmail(String to, String filename,
			String msgText, String subject) throws Exception {
		sendAttachmentEmail(to, FROM, HOST, filename, msgText, subject);
	}

	public static void sendAttachmentEmail(String to, String from, String host,
			String filename, String msgText, String subject) throws Exception {
		sendAttachmentEmail(new String[] { to }, from, host, filename, msgText, subject);
	}

	/**
	 * Sends an email, possibly with an attachment.
	 * If the filename parameter is null or the file doesn't exist then it won't be attached.
	 * @param to an array of the email addresses to
	 * @param from the from email address
	 * @param host the mail server
	 * @param filename the name of file to attach
	 * @param msgText the message body
	 * @param subject the subject of the email
	 * @throws Exception if an error occurred when sending the email
	 */
	public static void sendAttachmentEmail(String[] to, String from, String host,
			String filename, String msgText, String subject) throws Exception {
		// create some properties and get the default Session
		Properties props = System.getProperties();
		props.put("mail.smtp.host", host);

		Session session = Session.getInstance(props, null);
		session.setDebug(DEBUG);
		// create a message
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from));
		InternetAddress[] address = new InternetAddress[to.length];
		for (int i = 0; i < address.length; i++) {
			address[i] = new InternetAddress(to[i]);
		}
		msg.setRecipients(Message.RecipientType.TO, address);
		msg.setSubject(subject);

		// create and fill the first message part
		MimeBodyPart mbp1 = new MimeBodyPart();
		mbp1.setText(msgText);

		// create the Multipart and add its parts to it
		Multipart mp = new MimeMultipart();
		mp.addBodyPart(mbp1);

		// add the attachment
		if ((filename != null) && (filename.length() > 0) && new File(filename).exists()) {
			// create the second message part
			MimeBodyPart mbp2 = new MimeBodyPart();
			// attach the file to the message
			mbp2.attachFile(filename);
			mp.addBodyPart(mbp2);
		}

		// add the Multipart to the message
		msg.setContent(mp);

		// set the Date: header
		msg.setSentDate(new Date());

		// send the message
		Transport.send(msg);
	}

}
