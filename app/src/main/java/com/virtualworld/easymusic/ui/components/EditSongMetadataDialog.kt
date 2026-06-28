package com.virtualworld.easymusic.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.ui.theme.DarkBackground
import com.virtualworld.easymusic.ui.theme.Teal400
import com.virtualworld.easymusic.ui.theme.TextGray
import com.virtualworld.easymusic.ui.theme.TextWhite

@Composable
fun EditSongMetadataDialog(
    title: String,
    artist: String,
    album: String,
    aiLoading: Boolean,
    saving: Boolean,
    aiEnabled: Boolean,
    errorMessage: String?,
    onTitleChange: (String) -> Unit,
    onArtistChange: (String) -> Unit,
    onAlbumChange: (String) -> Unit,
    onFetchFromAi: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkBackground,
        titleContentColor = TextWhite,
        textContentColor = TextWhite,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.metadata_editor_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Teal400,
                )
                IconButton(
                    onClick = onFetchFromAi,
                    enabled = aiEnabled && !aiLoading && !saving,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (aiEnabled) Teal400 else TextGray,
                    ),
                ) {
                    if (aiLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Teal400,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = stringResource(R.string.cd_metadata_ai_fill),
                        )
                    }
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF8A80),
                    )
                }
                MetadataTextField(
                    value = title,
                    label = stringResource(R.string.metadata_field_title),
                    onValueChange = onTitleChange,
                    enabled = !saving,
                )
                MetadataTextField(
                    value = artist,
                    label = stringResource(R.string.metadata_field_artist),
                    onValueChange = onArtistChange,
                    enabled = !saving,
                )
                MetadataTextField(
                    value = album,
                    label = stringResource(R.string.metadata_field_album),
                    onValueChange = onAlbumChange,
                    enabled = !saving,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = !saving && !aiLoading,
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Teal400,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.metadata_save),
                        color = Teal400,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !saving,
            ) {
                Text(
                    text = stringResource(R.string.remove_from_queue_dialog_cancel),
                    color = TextGray,
                )
            }
        },
    )
}

@Composable
private fun MetadataTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            focusedBorderColor = Teal400,
            unfocusedBorderColor = TextGray,
            focusedLabelColor = Teal400,
            unfocusedLabelColor = TextGray,
            cursorColor = Teal400,
        ),
    )
}
