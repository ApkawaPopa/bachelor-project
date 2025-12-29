package minchakov.arkadii.amina.repository;

import minchakov.arkadii.amina.model.Chat;
import minchakov.arkadii.amina.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    Optional<Message> findFirstByChatOrderByCreatedAtDesc(Chat chat);

    List<Message> getMessagesByChat_IdOrderByCreatedAtAsc(Integer chatId);
}
