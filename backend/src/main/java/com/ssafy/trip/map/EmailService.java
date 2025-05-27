package com.ssafy.trip.map;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendTravelReminder(String toEmail, String planUuid) throws MessagingException {
        String url = "http://localhost:5173/plans/public/" + planUuid;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String subject = "RoomTraveler 여행 일정 알림";
        String htmlBody = "<p>안녕하세요! 오늘은 계획하셨던 여행일입니다.</p>"
                + "<p><a href=\"" + url + "\">여기에서 일정 보기</a>(금일동안 유효)</p>";

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        message.setFrom("noreply@ssafy.com");

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String username, String temporaryPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[비밀번호 재설정] 임시 비밀번호 발송");

            String emailContent = String.format(
                    "안녕하세요 %s님,\n\n" +
                            "요청하신 임시 비밀번호를 발송해드립니다.\n\n" +
                            "임시 비밀번호: %s\n\n" +
                            "보안을 위해 로그인 후 반드시 비밀번호를 변경해주세요.\n\n" +
                            "감사합니다.",
                    username, temporaryPassword
            );

            message.setText(emailContent);
            message.setFrom("noreply@ssafy.com");

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("이메일 전송 중 오류가 발생했습니다.", e);
        }
    }
}