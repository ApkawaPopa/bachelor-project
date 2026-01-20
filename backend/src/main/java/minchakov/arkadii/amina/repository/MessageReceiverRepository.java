package minchakov.arkadii.amina.repository;

import minchakov.arkadii.amina.model.MessageReceiver;
import minchakov.arkadii.amina.model.MessageReceiverId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReceiverRepository extends JpaRepository<MessageReceiver, MessageReceiverId> {
}
