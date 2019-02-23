package com.helwigdev.helpdesk.model

data class Location(
        val id: Int,
        val type: String?,
        val address: String?,
        val city: String?,
        val locationName: String?,
        val postalCode: String?,
        val state: String?
)

//"location": {
//    "id": 3,
//    "type": "Location",
//    "address": null,
//    "city": null,
//    "locationName": "ATL",
//    "postalCode": null,
//    "state": null
//},