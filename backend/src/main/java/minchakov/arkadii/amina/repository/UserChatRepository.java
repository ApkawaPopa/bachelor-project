package minchakov.arkadii.amina.repository;

import minchakov.arkadii.amina.model.Chat;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.model.UserChat;
import minchakov.arkadii.amina.model.UserChatId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserChatRepository extends JpaRepository<UserChat, UserChatId> {
    boolean existsByUserAndChat(User currentUser, Chat requestedChat);

    Optional<UserChat> findByUser_IdAndChat_Id(Integer userId, Integer chatId);

    boolean existsByUserAndChat_Id(User user, int chatId);

}
