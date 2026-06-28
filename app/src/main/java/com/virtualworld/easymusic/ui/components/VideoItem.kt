package com.virtualworld.easymusic.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.domain.model.Video
import com.virtualworld.easymusic.ui.theme.Teal400
import com.virtualworld.easymusic.ui.theme.TextGray

private val ItemHorizontalPadding = 19.dp
private val ItemVerticalPadding = 12.dp
private val ThumbnailWidth = 107.dp
private val ThumbnailHeight = 60.dp
private val ItemSpacing = 17.dp
private val ThumbnailCornerRadius = 10.dp
private val FavoriteIconSize = 26.dp

@Composable
fun VideoItem(
    video: Video,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = ItemHorizontalPadding, vertical = ItemVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VideoThumbnailAsyncImage(
            videoId = video.id,
            videoUri = video.uri,
            contentDescription = video.title,
            modifier = Modifier
                .width(ThumbnailWidth)
                .height(ThumbnailHeight)
                .clip(RoundedCornerShape(ThumbnailCornerRadius)),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(ItemSpacing))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
            )
            if (video.width > 0 && video.height > 0) {
                Text(
                    text = "${video.width}×${video.height}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    maxLines = 1,
                )
            }
        }

        Text(
            text = formatDuration(video.duration),
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
        )

        if (onToggleFavorite != null) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = if (isFavorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = if (isFavorite) {
                        stringResource(R.string.cd_remove_favorite)
                    } else {
                        stringResource(R.string.cd_add_favorite)
                    },
                    modifier = Modifier.size(FavoriteIconSize),
                    tint = if (isFavorite) Teal400 else TextGray,
                )
            }
        }
    }
}
