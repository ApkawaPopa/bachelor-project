package minchakov.arkadii.amina.dto;

import jakarta.validation.Valid;

import java.util.List;

public record GetUsersKeysInDTO(@Valid List<GetUsersKeysUsernameDTO> usernames) {
}
