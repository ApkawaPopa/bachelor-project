package minchakov.arkadii.amina.repository;

import minchakov.arkadii.amina.model.S3Object;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface S3ObjectRepository extends JpaRepository<S3Object, Integer> {
    void deleteByMessageIsNullAndCreatedAtIsBefore(LocalDateTime createdAtBefore);

    List<S3Object> findByMessage_Id(int messageId);
}
