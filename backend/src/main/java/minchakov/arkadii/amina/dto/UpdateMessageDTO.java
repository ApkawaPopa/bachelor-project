package minchakov.arkadii.amina.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateMessageDTO {
    @NotBlank(message = "This field cannot be blank")
    String content;

    public UpdateMessageDTO() {
    }

    public UpdateMessageDTO(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
