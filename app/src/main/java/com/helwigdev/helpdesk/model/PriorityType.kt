package com.helwigdev.helpdesk.model

data class PriorityType(
        val id: Int,
        val type: String?,
        val priorityTypeName: String?
)

//"prioritytype": {
//    "id": 3,
//    "type": "PriorityType",
//    "priorityTypeName": "Medium"
//},