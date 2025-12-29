package minchakov.arkadii.amina.dto;

import jakarta.validation.constraints.NotBlank;

public class InMessageDTO {
    @NotBlank(message = "Field cannot be blank")
    private String content;

    public InMessageDTO() {
    }

    public InMessageDTO(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
