package minchakov.arkadii.amina.dto;

import java.net.URL;

public record ChatPictureChangedEvent(int chatId, URL pictureUrl) {
}