package minchakov.arkadii.amina.repository;

import minchakov.arkadii.amina.model.Chat;
import minchakov.arkadii.amina.model.S3Object;
import minchakov.arkadii.amina.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface S3ObjectRepository extends JpaRepository<S3Object, Integer> {

    @Modifying
    @Query("select o.id from S3Object o where o.createdAt < :createdAt and o.confirmedAt is null")
    List<Integer> getUnconfirmedObjectsIds(LocalDateTime createdAt);

    List<S3Object> findAllByChat(Chat chat);

    List<S3Object> findAllByUser(User user);
}
