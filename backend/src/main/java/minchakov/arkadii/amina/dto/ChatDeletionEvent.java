package minchakov.arkadii.amina.dto;

import java.util.List;

public record ChatDeletionEvent(int chatId, List<Integer> userIds) {
}
