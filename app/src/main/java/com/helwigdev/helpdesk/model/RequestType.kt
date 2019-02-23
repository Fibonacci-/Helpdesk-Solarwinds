package com.helwigdev.helpdesk.model

data class RequestType(
        val id: Int,
        val type: String?,
        val detailDisplayName: String?
)

//"problemtype": {
//    "id": 140,
//    "type": "RequestType",
//    "detailDisplayName": "IT Request &#8226; General"
//},