package minchakov.arkadii.amina.jobs;

import minchakov.arkadii.amina.service.S3Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CleanUnconfirmedObjectsJob {

    private final S3Service s3Service;

    public CleanUnconfirmedObjectsJob(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanUnconfirmedObjects() {
        s3Service.cleanUnconfirmedObjectsBefore(LocalDateTime.now().minusHours(1));
    }
}
