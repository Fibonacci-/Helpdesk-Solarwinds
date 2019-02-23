package com.helwigdev.helpdesk.model

data class Room(
        val id: Int,
        val type: String?,
        val roomName: String?,
        val locationId: Int?
)