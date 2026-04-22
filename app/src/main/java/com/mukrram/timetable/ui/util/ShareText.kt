package com.mukrram.timetable.ui.util

import android.content.Context
import android.content.Intent

fun sharePlainText(context: Context, chooserTitle: String, subject: String, body: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    val chooser = Intent.createChooser(send, chooserTitle)
    context.startActivity(chooser)
}
