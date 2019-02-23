package com.helwigdev.helpdesk.model

data class Client(
        val id: Int,
        val type: String?,
        val email: String?,
        val firstName: String?,
        val lastName: String?,
        val notes: String?,
        val phone: String?,
        val phone2: String?,
        val department: Department?,
        val location: Location?,
        val room: Room?,
        val companyName: String?,
        val username: String?

)

//"clientReporter": {
//    "id": 1,
//    "type": "Client",
//    "email": "client@solarwinds.com",
//    "firstName": "Demo",
//    "lastName": "Client",
//    "notes": null,
//    "phone": "+1 512-682-9300",
//    "phone2": "+1 512-682-9300",
//    "department": null,
//    "location": {
//        "id": 3,
//        "type": "Location",
//        "address": null,
//        "city": null,
//        "locationName": "ATL",
//        "postalCode": null,
//        "state": null
//    },
//    "room": null,
//    "companyName": "Big Company",
//    "username": "client"
//},