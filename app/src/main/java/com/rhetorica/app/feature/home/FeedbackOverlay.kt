package com.rhetorica.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rhetorica.app.R

/**
 * Feedback icon and compose sheet. Place it in the slot [FeedbackPlacement]
 * names for the current screen — top bar actions, or the end of a title row —
 * so it stays in layout flow and cannot cover bottom controls.
 *
 * Submit hands off to the user's mail app; Rhetorica does not collect the message.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackAction(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf("") }
    var mailError by rememberSaveable { mutableStateOf(false) }
    val address = stringResource(R.string.feedback_destination_email)
    val subject = stringResource(R.string.feedback_subject)
    val sendFeedbackCd = stringResource(R.string.feedback_cd)

    IconButton(
        onClick = {
            sheetOpen = true
            mailError = false
        },
        modifier = modifier.semantics { contentDescription = sendFeedbackCd },
    ) {
        Icon(
            imageVector = Icons.Outlined.Feedback,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
    }

    if (sheetOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                sheetOpen = false
                message = ""
                mailError = false
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.feedback_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = {
                        message = it
                        mailError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    minLines = 4,
                    label = { Text(stringResource(R.string.feedback_message_label)) },
                )
                if (mailError) {
                    Text(
                        text = stringResource(R.string.feedback_mail_missing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Button(
                    onClick = {
                        val launched = FeedbackMail.launch(
                            context = context,
                            address = address,
                            subject = subject,
                            body = message.trim(),
                        )
                        if (launched) {
                            sheetOpen = false
                            message = ""
                            mailError = false
                        } else {
                            mailError = true
                        }
                    },
                    enabled = FeedbackMail.hasMessage(message),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.feedback_submit))
                }
            }
        }
    }
}

/** Top-end feedback control for screens whose body has no title row yet. */
@Composable
fun FeedbackEndRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FeedbackAction()
    }
}
