package minchakov.arkadii.amina.jobs;

import minchakov.arkadii.amina.repository.S3ObjectRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CleanUnconfirmedObjectsJob {

    private final S3ObjectRepository s3ObjectRepository;

    public CleanUnconfirmedObjectsJob(S3ObjectRepository s3ObjectRepository) {
        this.s3ObjectRepository = s3ObjectRepository;
    }

    @Scheduled(cron = "0 0 /6 * * *")
    public void cleanUnconfirmedObjects() {
        s3ObjectRepository.deleteByMessageIsNullAndCreatedAtIsBefore(LocalDateTime.now().minusHours(6));
    }
}
