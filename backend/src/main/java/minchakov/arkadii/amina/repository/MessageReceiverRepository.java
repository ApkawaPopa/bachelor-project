package minchakov.arkadii.amina.repository;

import minchakov.arkadii.amina.model.Chat;
import minchakov.arkadii.amina.model.MessageReceiver;
import minchakov.arkadii.amina.model.MessageReceiverId;
import minchakov.arkadii.amina.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReceiverRepository extends JpaRepository<MessageReceiver, MessageReceiverId> {
    long countByChatAndReceiver(Chat chat, User receiver);
}
