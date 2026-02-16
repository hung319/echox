package com.telegram.videoplayer.domain.model

data class User(
    val id: Long,
    val phoneNumber: String,
    val firstName: String?,
    val lastName: String?,
    val username: String?
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ")
}
