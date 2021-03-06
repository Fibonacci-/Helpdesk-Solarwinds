package com.helwigdev.helpdesk.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class Ticket(
        val id: Int,
        val type: String?,
        val bccAddresses: String?,
        val ccAddressesForTech: String?,
        val closeDate: String?,
        val departmentId: Int?,
        val lastUpdated: Date?,
        val locationId: Int?,
        val priorityTypeId: Int?,
        val room: String?,
        val subject: String?,
        val shortSubject: String?,
        val clientReporter: Client?,
        val clientTech: Tech?,
        val department: Department?,
        val location: Location?,
        @SerializedName("prioritytype") val priorityType: PriorityType?,
        @SerializedName("problemtype") val problemType: RequestType?,
        @SerializedName("statustype") val statusType: StatusType?,
        val techGroupLevel: TechGroupLevel?,
        val assets: Array<Asset>?,
        val detail: String?,
        val shortDetail: String?,
        val reportDateUtc: Date?,
        val displayDueDate: Date?,
        val displayClient: String?,
        val emailClient: Boolean = false,
        val emailTech: Boolean = false,
        val emailTechGroupLevel: Boolean = false,
        val emailGroupManager: Boolean = false,
        val emailCc: Boolean = false,
        val emailBcc: Boolean = false,
        val needsApproval: Boolean?,
        val ticketEditable: Boolean?,
        val techId: Int?,
        val levelNumber: String?,
        val clientId: Int?,
        val flaggedByTech: Boolean?,
        val isPublic: Boolean?,
        val canEscalate: Boolean?,
        val bookmarkableLink: String?,
        val isDeleted: Boolean?,
        val notes: Array<Note>?,
        val enabledStatusTypes: Array<StatusType>?,
        val updateFlagType: Int?,
        val prettyLastUpdated: String?,
        val latestNote: Note?
)

//still need ticketCustomFields[] and attachments[]