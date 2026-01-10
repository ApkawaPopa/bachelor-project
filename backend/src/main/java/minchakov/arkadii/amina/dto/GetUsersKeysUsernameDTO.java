package minchakov.arkadii.amina.dto;

import minchakov.arkadii.amina.validator.GetUsersKeysUsernameConstraint;

public record GetUsersKeysUsernameDTO(@GetUsersKeysUsernameConstraint String username) {
}
