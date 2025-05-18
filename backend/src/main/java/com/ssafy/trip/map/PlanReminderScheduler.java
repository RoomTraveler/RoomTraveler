package com.ssafy.trip.map;

import com.ssafy.trip.map.MapDTO.EmailPlanDTO;
import jakarta.mail.MessagingException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanReminderScheduler {

    private final MapDAO mapDAO;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 0 8 * * ?")
    public void sendReminders() throws MessagingException {
        List<EmailPlanDTO> todayPlans = mapDAO.selectPlansForToday();

        for (EmailPlanDTO plan : todayPlans) {
            String uuid = UUID.randomUUID().toString();

            redisTemplate.opsForValue().set(
                    uuid,
                    String.valueOf(plan.getPlanId()),
                    Duration.ofHours(16)
            );

            emailService.sendTravelReminder(plan.getEmail(), uuid);
        }
    }
}
