package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageEntity
import com.example.ui.theme.BlueCheckMark
import com.example.ui.theme.DarkBgMain
import com.example.ui.theme.DarkSurfaceHeader
import com.example.ui.theme.DarkIncomingBubble
import com.example.ui.theme.DarkOutgoingBubble
import com.example.ui.theme.DisappearingFire
import com.example.ui.theme.KachehriGreenPrimary
import com.example.ui.theme.LightIncomingBubble
import com.example.ui.theme.LightOutgoingBubble
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: MessageEntity,
    currentTimeMillis: Long,
    onViewOnceClick: (MessageEntity) -> Unit,
    onInspectSecurityClick: (MessageEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOutgoing = message.isOutgoing
    val isDark = MaterialTheme.colorScheme.background == DarkBgMain ||
            MaterialTheme.colorScheme.surface == DarkSurfaceHeader

    val bubbleBg = when {
        isOutgoing && isDark -> DarkOutgoingBubble
        isOutgoing && !isDark -> LightOutgoingBubble
        !isOutgoing && isDark -> DarkIncomingBubble
        else -> LightIncomingBubble
    }

    val bubbleTextColor = when {
        isOutgoing && !isDark -> Color(0xFF111B21)
        !isOutgoing && !isDark -> Color(0xFF111B21)
        else -> Color(0xFFE9EDEF)
    }

    val metaTextColor = when {
        isOutgoing && !isDark -> Color(0xFF536774)
        !isOutgoing && !isDark -> Color(0xFF667781)
        else -> Color(0xFF8696A0)
    }

    // Disappearing time remaining
    val remainingSeconds = if (message.expiresAt != null) {
        ((message.expiresAt - currentTimeMillis) / 1000).coerceAtLeast(0)
    } else null

    val isExpiringSoon = remainingSeconds != null && remainingSeconds in 1..5

    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    val borderStroke = if (isExpiringSoon) {
        BorderStroke(1.5.dp, DisappearingFire)
    } else null

    val alignment = if (isOutgoing) Alignment.End else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = bubbleShape,
            color = bubbleBg,
            shadowElevation = 1.dp,
            border = borderStroke,
            modifier = Modifier
                .widthIn(min = 90.dp, max = 310.dp)
                .clickable { onInspectSecurityClick(message) }
                .testTag("message_bubble_${message.id}")
        ) {
            Column(modifier = Modifier.padding(8.dp)) {

                // View Once Media
                if (message.isViewOnce) {
                    ViewOnceCard(
                        message = message,
                        onClick = { onViewOnceClick(message) }
                    )
                } else if (message.mediaType == "PHOTO" && message.mediaResName != null) {
                    // Regular media photo
                    NormalPhotoCard(message = message)
                } else if (message.mediaType == "VOICE") {
                    // Simulated Voice Note
                    VoiceNotePlayer(durationSec = message.voiceDurationSeconds)
                } else {
                    // Text message
                    Text(
                        text = message.text,
                        color = bubbleTextColor,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Metadata row (E2EE lock, disappearing timer, timestamp, checkmarks)
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    // E2EE Lock icon
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = metaTextColor.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(11.dp)
                            .padding(end = 3.dp)
                    )

                    // Disappearing countdown badge
                    if (remainingSeconds != null) {
                        val countdownText = when {
                            remainingSeconds > 3600 -> "${remainingSeconds / 3600}h"
                            remainingSeconds > 60 -> "${remainingSeconds / 60}m"
                            else -> "${remainingSeconds}s"
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isExpiringSoon) DisappearingFire.copy(alpha = 0.2f) else Color.Transparent)
                                .padding(horizontal = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Disappearing timer",
                                tint = if (isExpiringSoon) DisappearingFire else metaTextColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = countdownText,
                                fontSize = 10.sp,
                                fontWeight = if (isExpiringSoon) FontWeight.Bold else FontWeight.Normal,
                                color = if (isExpiringSoon) DisappearingFire else metaTextColor,
                                modifier = Modifier.padding(start = 2.dp, end = 4.dp)
                            )
                        }
                    }

                    // Timestamp
                    Text(
                        text = formatTime(message.timestamp),
                        fontSize = 11.sp,
                        color = metaTextColor
                    )

                    // Delivery checkmarks for outgoing
                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = BlueCheckMark,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewOnceCard(
    message: MessageEntity,
    onClick: () -> Unit
) {
    val isOpened = message.isOpened

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isOpened) Color.Black.copy(alpha = 0.08f) else KachehriGreenPrimary.copy(alpha = 0.12f),
        border = BorderStroke(
            1.dp,
            if (isOpened) Color.Gray.copy(alpha = 0.3f) else KachehriGreenPrimary.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !isOpened, onClick = onClick)
            .padding(2.dp)
            .testTag("view_once_card")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle with "1" inside
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isOpened) Color.Gray.copy(alpha = 0.25f) else KachehriGreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "①",
                    color = if (isOpened) Color.DarkGray else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = if (isOpened) "Opened" else "Photo (View Once)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                    color = if (isOpened) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isOpened) "Media burned from memory" else "Tap to view • Self-destructs",
                    fontSize = 11.5.sp,
                    color = if (isOpened) Color.Gray.copy(alpha = 0.8f) else KachehriGreenPrimary
                )
            }
        }
    }
}

@Composable
private fun NormalPhotoCard(message: MessageEntity) {
    val context = LocalContext.current
    val resId = remember(message.mediaResName) {
        if (message.mediaResName != null) {
            context.resources.getIdentifier(message.mediaResName, "drawable", context.packageName)
        } else 0
    }

    Column {
        if (resId != 0) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = "Sent photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
        if (message.text.isNotBlank() && !message.text.startsWith("[")) {
            Text(
                text = message.text,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
            )
        }
    }
}

@Composable
private fun VoiceNotePlayer(durationSec: Int) {
    var isPlaying by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .size(38.dp)
                .background(KachehriGreenPrimary, CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Waveform bars
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val barHeights = listOf(14, 22, 10, 26, 18, 12, 28, 16, 24, 18, 10, 20, 25, 12, 18, 15)
            barHeights.forEachIndexed { index, h ->
                val active = isPlaying && index < 10
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(h.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(if (active) KachehriGreenPrimary else Color.Gray.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "0:${if (durationSec < 10) "0$durationSec" else "$durationSec"}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTime(millis: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}
