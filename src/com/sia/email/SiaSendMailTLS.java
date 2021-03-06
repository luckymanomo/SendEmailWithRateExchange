package com.sia.email;
import java.util.Date;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SiaSendMailTLS {
	final static String username = "luckymanomo2@gmail.com";
	final static String password = "aaaAAA111";
	
	//final static String toEmails="luckymanomo@gmail.com";
	public static Properties props;
	public static void sendMessage(String subject,String textBody,String toEmails){
		try {
			Transport.send(initialMail(subject,textBody,toEmails));
			System.out.println("Done at "+new Date());
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
	public static Message initialMail(String subject,String textBody,String toEmails) throws AddressException, MessagingException{
		props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress("evil@gmail.com"));
		message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(toEmails));
		//message.setRecipients(Message.RecipientType.TO,InternetAddress.parse("luckymanomo@gmail.com"));
		message.setSubject(subject);
		BodyPart messageBodyPart = new MimeBodyPart(); 
		messageBodyPart.setContent(textBody, "text/html; charset=utf-8" ); 
		Multipart multipart = new MimeMultipart(); 
		multipart.addBodyPart(messageBodyPart);
		message.setContent(multipart);  
		//message.setText(textBody);

		return message;

		
	}
}
