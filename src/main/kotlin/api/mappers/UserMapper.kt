package api.mappers

import api.dto.UserResponse
import domain.model.User

fun User.toResponse() = UserResponse(
    id = id,
    username = username,
    email = email,
    role = role
)
