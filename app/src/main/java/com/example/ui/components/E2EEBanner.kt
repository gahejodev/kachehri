package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DarkBgMain
import com.example.ui.theme.DarkSurfaceHeader
import com.example.ui.theme.E2EEAmber
import com.example.ui.theme.E2EEAmberBgDark
import com.example.ui.theme.E2EEAmberBgLight

@Composable
fun E2EEBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBgMain ||
            MaterialTheme.colorScheme.surface == DarkSurfaceHeader
    val bgColor = if (isDark) E2EEAmberBgDark else E2EEAmberBgLight
    val textColor = if (isDark) E2EEAmber else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("e2ee_security_banner"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Security notice",
                tint = E2EEAmber,
                modifier = Modifier
                    .size(15.dp)
                    .padding(end = 4.dp)
            )
            Text(
                text = "Security notice: This prototype does not provide verified end-to-end encryption. Do not share sensitive messages.",
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
