package minchakov.arkadii.amina.repository;

import minchakov.arkadii.amina.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Integer> {
}
