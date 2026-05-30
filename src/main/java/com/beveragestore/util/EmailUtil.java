package com.beveragestore.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class tiện ích dùng để gửi email chứa khóa bí mật cho khách hàng.
 * chạy bất đồng bộ để tránh làm chậm luồng sinh khóa của user.
 */
public class EmailUtil {
    private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

    // các cấu hình smtp mặc định (có thể đổi qua gmail)
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String FROM_EMAIL = System.getenv("SMTP_EMAIL");
    private static final String FROM_PASSWORD = System.getenv("SMTP_PASSWORD");

    /**
     * gửi email bất đồng bộ chứa file khóa bí mật pem.
     */
    public static void sendPrivateKeyEmailAsync(String toEmail, String keyId, String privateKeyPem) {
        new Thread(() -> {
            try {
                sendPrivateKeyEmail(toEmail, keyId, privateKeyPem);
            } catch (Exception e) {
                logger.error("lỗi khi gửi email chứa private key tới " + toEmail, e);
                // in ra console log của tomcat phòng trường hợp không cấu hình được smtp thực tế
                System.out.println("==================================================");
                System.out.println("KHÔNG THỂ GỬI EMAIL SMTP. THÔNG TIN KHÓA:");
                System.out.println("Gửi tới: " + toEmail);
                System.out.println("Key ID: " + keyId);
                System.out.println("Khóa bí mật (Private Key PEM):\n" + privateKeyPem);
                System.out.println("==================================================");
            }
        }).start();
    }

    private static void sendPrivateKeyEmail(String toEmail, String keyId, String privateKeyPem) throws Exception {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", SMTP_HOST);
        prop.put("mail.smtp.port", SMTP_PORT);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL, "The Grindery Coffee"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Khóa bí mật (Private Key) đặt hàng của bạn");

        // tạo nội dung email đa phần (multipart) để đính kèm file
        Multipart multipart = new MimeMultipart();

        // phần nội dung text
        MimeBodyPart textBodyPart = new MimeBodyPart();
        String htmlContent = "<h3>Xin chào,</h3>"
                + "<p>Bạn vừa sinh mới một cặp khóa chữ ký số trên hệ thống <strong>The Grindery</strong>.</p>"
                + "<p>Dưới đây là thông tin khóa bí mật (Private Key) của bạn dùng để ký số đơn hàng khi thanh toán:</p>"
                + "<pre style='background:#f4f4f4; padding:10px; border:1px solid #ddd;'>" + privateKeyPem + "</pre>"
                + "<p><strong>Chú ý bảo mật:</strong> Không chia sẻ khóa này cho bất kỳ ai. Chúng tôi cũng đã đính kèm file <code>private_key.pem</code> dưới email này để bạn tiện sử dụng tải lên.</p>"
                + "<p>Trân trọng,<br>Đội ngũ The Grindery</p>";
        textBodyPart.setContent(htmlContent, "text/html; charset=UTF-8");
        multipart.addBodyPart(textBodyPart);

        // phần đính kèm file .pem
        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        attachmentBodyPart.setFileName("private_key_" + keyId.substring(0, 8) + ".pem");
        attachmentBodyPart.setContent(privateKeyPem, "text/plain; charset=UTF-8");
        multipart.addBodyPart(attachmentBodyPart);

        message.setContent(multipart);
        Transport.send(message);
        logger.info("đã gửi email chứa private key thành công tới {}", toEmail);
    }
}
