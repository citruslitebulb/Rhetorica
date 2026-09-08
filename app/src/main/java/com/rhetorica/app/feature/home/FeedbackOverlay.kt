package com.rhetorica.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rhetorica.app.R

/**
 * App-wide feedback chip and compose sheet. Submit hands off to the user's
 * mail app; Rhetorica does not collect the message.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackOverlay(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf("") }
    var mailError by rememberSaveable { mutableStateOf(false) }
    val address = stringResource(R.string.feedback_destination_email)
    val subject = stringResource(R.string.feedback_subject)
    val sendFeedbackCd = stringResource(R.string.feedback_cd)

    AssistChip(
        onClick = {
            sheetOpen = true
            mailError = false
        },
        modifier = modifier.semantics { contentDescription = sendFeedbackCd },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface,
            leadingIconContentColor = MaterialTheme.colorScheme.primary,
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = MaterialTheme.colorScheme.outline,
        ),
        label = { Text(text = stringResource(R.string.feedback_chip)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Feedback,
                contentDescription = null,
            )
        },
    )

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
