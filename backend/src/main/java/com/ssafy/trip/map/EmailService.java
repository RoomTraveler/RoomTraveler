package com.ssafy.trip.map;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendTravelReminder(String toEmail, String planUuid) throws MessagingException {
        String url = "http://localhost:8080/map/plans/public/" + planUuid;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String subject = "RoomTraveler 여행 일정 알림";
        String htmlBody = "<p>안녕하세요! 오늘은 계획하셨던 여행일입니다.</p>"
                + "<p><a href=\"" + url + "\">여기에서 일정 보기</a>(금일동안 유효)</p>";

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }
}