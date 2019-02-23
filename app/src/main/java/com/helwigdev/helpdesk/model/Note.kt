package com.helwigdev.helpdesk.model

import java.util.*

data class Note(
        val id: Int,
        val type: String?,
        val date: Date?,
        val isSolution: Boolean?,
        val prettyUpdatedString: String?,
        val mobileNoteText: String?,
        val isTechNote: Boolean?,
        val noteColor: String?,
        val noteClass: String?
        )

//TODO long note on click?
//"id": 35,
//"type": "TechNote",
//"date": "2019-02-12T08:25:23Z",
//"isSolution": false,
//"prettyUpdatedString": "2 weeks ago <strong>Joe Admin</strong> said",
//"mobileNoteText": "New deadline: the next Friday.",
//"isTechNote": true,
//"isHidden": false,
//"attachments": []