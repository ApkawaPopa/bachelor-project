package minchakov.arkadii.amina.model;

import java.io.Serializable;
import java.util.Objects;

public class MessageReceiverId implements Serializable {

    private Message message;

    private User receiver;

    public MessageReceiverId() {
    }

    public MessageReceiverId(Message message, User receiver) {
        this.message = message;
        this.receiver = receiver;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessageReceiverId that))
            return false;
        return Objects.equals(message, that.message) && Objects.equals(receiver, that.receiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, receiver);
    }
}
