package com.helwigdev.helpdesk.model

data class Tech(
        val id: Int,
        val type: String?,
        val email: String?,
        val displayName: String?
)

//"clientTech": {
//    "id": 1,
//    "type": "Tech",
//    "email": "joe.admin@webhelpdesk.com",
//    "displayName": "Joe Admin"
//},