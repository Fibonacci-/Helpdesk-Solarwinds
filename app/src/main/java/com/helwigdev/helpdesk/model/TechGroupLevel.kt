package com.helwigdev.helpdesk.model

data class TechGroupLevel(
        val id: Int,
        val type: String?,
        val level: Int?,
        val levelName: String?,
        val shortLevelName: String?
)

//"techGroupLevel": {
//    "id": 13,
//    "type": "TechGroupLevel",
//    "level": 1,
//    "levelName": "IT Network Support&nbsp;&nbsp;Level 1",
//    "shortLevelName": "Level 1"
//},