package com.helwigdev.helpdesk.model

data class StatusType(
        val id: Int,
        val type: String?,
        val statusTypeName: String?
){
    override fun toString(): String {
        return this.statusTypeName ?: super.toString()
    }
}

//"statustype": {
//    "id": 1,
//    "type": "StatusType",
//    "statusTypeName": "Open"
//},