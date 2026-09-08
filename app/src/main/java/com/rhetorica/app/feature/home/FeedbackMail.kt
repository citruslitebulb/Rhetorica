package com.rhetorica.app.feature.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Builds a mailto handoff. Rhetorica does not send or store the message.
 */
object FeedbackMail {
    fun hasMessage(text: String): Boolean = text.trim().isNotEmpty()

    fun mailtoUri(address: String, subject: String, body: String): String {
        val encodedSubject = encodeQuery(subject)
        val encodedBody = encodeQuery(body)
        return "mailto:$address?subject=$encodedSubject&body=$encodedBody"
    }

    fun launch(context: Context, address: String, subject: String, body: String): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse(mailtoUri(address, subject, body))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    private fun encodeQuery(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
    }
}
