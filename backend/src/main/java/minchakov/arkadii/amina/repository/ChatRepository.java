package minchakov.arkadii.amina.repository;

import minchakov.arkadii.amina.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
}
