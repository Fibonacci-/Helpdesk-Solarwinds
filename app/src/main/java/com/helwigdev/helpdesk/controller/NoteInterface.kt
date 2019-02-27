package com.helwigdev.helpdesk.controller

import com.helwigdev.helpdesk.model.FullNote

interface NoteInterface {
    fun noteSendResult()
    fun noteSendError(error: String)
}