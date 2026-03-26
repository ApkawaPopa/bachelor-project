package minchakov.arkadii.amina.model;

import java.io.Serializable;
import java.util.Objects;

public class MessageReceiverId implements Serializable {

    private Integer message;

    private Integer receiver;

    public MessageReceiverId() {
    }

    public MessageReceiverId(Integer message, Integer receiver) {
        this.message = message;
        this.receiver = receiver;
    }

    public Integer getMessage() {
        return message;
    }

    public void setMessage(Integer message) {
        this.message = message;
    }

    public Integer getReceiver() {
        return receiver;
    }

    public void setReceiver(Integer receiver) {
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
