package minchakov.arkadii.amina.repository;

import minchakov.arkadii.amina.model.UserChat;
import minchakov.arkadii.amina.model.UserChatId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChatRepository extends JpaRepository<UserChat, UserChatId> {
}
