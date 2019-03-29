package com.jdbc.post;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;
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
import org.springframework.stereotype.Component;


@Component
public class MailSender {
	
	public MailSender() {
		
	}
	
	
	private Properties getProps() {
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		return props;
	}
	
	
	public void sendEmail(String subject) throws AddressException, MessagingException, IOException {
		
		ResourceBundle rb = ResourceBundle.getBundle("notifications.email");
    	String emailFromUsername = rb.getString("email.from.username");
    	String emailFromPass = rb.getString("email.from.pass");
    	String emailToAddress = rb.getString("email.no.notify");
    			
		Session session = Session.getInstance(getProps(), new javax.mail.Authenticator() {
		      protected PasswordAuthentication getPasswordAuthentication() {
		         return new PasswordAuthentication(emailFromUsername, emailFromPass);
		      }
		   });
		   Message msg = new MimeMessage(session);
		   msg.setFrom(new InternetAddress(emailFromUsername, false));  
		   msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailToAddress));
		   msg.setSubject(subject);
		   msg.setSentDate(new Date()); // TODO how to cater for LocalDate???
		   
		   
		   MimeBodyPart messageBodyPart = new MimeBodyPart();
		   messageBodyPart.setContent("Some basic email blar blar", "text/html");
		   Multipart multipart = new MimeMultipart();
		   multipart.addBodyPart(messageBodyPart);

		   // add an attachment
//		   MimeBodyPart attachPart = new MimeBodyPart();
//		   attachPart.attachFile("/var/tmp/image19.png");
//		   multipart.addBodyPart(attachPart);

		   msg.setContent(multipart);    // ALTERNATIVE msg.setContent(subject + " blarblarblar", "text/html");
		   Transport.send(msg);
		   
	}
	
	
	
}
