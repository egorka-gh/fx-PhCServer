	package com.photodispatcher.mail;
	
	import java.util.Properties;
	
	import javax.mail.Message;
	import javax.mail.MessagingException;
	import javax.mail.PasswordAuthentication;
	import javax.mail.Session;
	import javax.mail.Transport;
	import javax.mail.internet.InternetAddress;
	import javax.mail.internet.MimeMessage;
	
	public class SendMailByGoogle {
		
		public static boolean send(String from, String to, String subject, String text) {
			
			final String username = "username@gmail.com";
			final String password = "********";
			
			Properties props = new Properties();
			
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");
			
			Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}});
			
			try {
				
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(from));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
				message.setSubject(subject);
				message.setText(text);
				
				Transport.send(message);
				
				return true;
				
			} catch (MessagingException e) {
				
				throw new RuntimeException(e);
				
			}
			
		}
		
	}
