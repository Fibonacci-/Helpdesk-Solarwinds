package com.helwigdev.helpdesk.model

//required fields: jobticket, noteText, statusTypeId, workTime
data class FullNote(
    var bccAddresses: String?,
    var emailBcc: Boolean?,
    var emailCc: Boolean?,
    var emailClient: Boolean?,
    var emailGroupManager: Boolean?,
    var emailTech: Boolean?,
    var emailTechGroupLevel: Boolean?,
    var isHidden: Boolean?,
    var isSolution: Boolean?,
    var jobticket: JobTicket,
    var noteText: String,
    var statusTypeId: Int,
    var workTime: String
)

data class JobTicket(
        var id: Int,
        var type: String = "JobTicket"
)
