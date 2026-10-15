package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactEntity
import com.example.data.model.MessageEntity
import com.example.ui.components.E2EEBanner
import com.example.ui.components.MessageBubble
import com.example.ui.theme.DarkBgMain
import com.example.ui.theme.DarkChatBackground
import com.example.ui.theme.DarkSurfaceHeader
import com.example.ui.theme.DisappearingFire
import com.example.ui.theme.KachehriGreenDark
import com.example.ui.theme.KachehriGreenPrimary
import com.example.ui.theme.LightChatBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contact: ContactEntity,
    messages: List<MessageEntity>,
    currentTimeMillis: Long,
    onBackClick: () -> Unit,
    onSendMessage: (text: String, isViewOnce: Boolean, mediaType: String, mediaResName: String?, voiceSec: Int) -> Unit,
    onViewOnceClick: (MessageEntity) -> Unit,
    onInspectSecurityClick: (MessageEntity) -> Unit,
    onVerifySafetyNumberClick: () -> Unit,
    onDisappearingSettingsClick: () -> Unit,
    onStartCallClick: (isVideo: Boolean) -> Unit,
    onClearChatClick: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var isViewOnceActive by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val isDark = MaterialTheme.colorScheme.background == DarkBgMain ||
            MaterialTheme.colorScheme.surface == DarkSurfaceHeader
    val chatBg = if (isDark) DarkChatBackground else LightChatBackground

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVerifySafetyNumberClick() }
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(contact.avatarColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contact.name.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = contact.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (contact.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Safety Number",
                                        tint = KachehriGreenPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (contact.disappearingTimerSeconds > 0) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Disappearing timer active",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    val timerLabel = when (contact.disappearingTimerSeconds) {
                                        10 -> "10s timer"
                                        86400 -> "24h timer"
                                        604800 -> "7d timer"
                                        else -> "${contact.disappearingTimerSeconds}s"
                                    }
                                    Text(
                                        text = "$timerLabel • ",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                                Text(
                                    text = contact.lastSeen,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onStartCallClick(false) }, modifier = Modifier.testTag("audio_call_button")) {
                        Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = Color.White)
                    }
                    IconButton(onClick = { onStartCallClick(true) }, modifier = Modifier.testTag("video_call_button")) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
                    }
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }, modifier = Modifier.testTag("chat_menu_button")) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Verify Safety Number") },
                                leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onVerifySafetyNumberClick()
                                },
                                modifier = Modifier.testTag("menu_verify_safety_number")
                            )
                            DropdownMenuItem(
                                text = { Text("Disappearing Messages") },
                                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onDisappearingSettingsClick()
                                },
                                modifier = Modifier.testTag("menu_disappearing_messages")
                            )
                            DropdownMenuItem(
                                text = { Text("Clear Chat") },
                                leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onClearChatClick()
                                },
                                modifier = Modifier.testTag("menu_clear_chat")
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF111B21) else KachehriGreenDark
                )
            )
        },
        bottomBar = {
            // Input Row
            Column(modifier = Modifier.background(if (isDark) Color(0xFF111B21) else Color(0xFFF0F2F5))) {
                // View once active banner indicator
                AnimatedVisibility(visible = isViewOnceActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KachehriGreenPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(KachehriGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("①", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Once mode active: media disappears after recipient closes it",
                            color = KachehriGreenPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment button
                    IconButton(
                        onClick = { showAttachmentSheet = true },
                        modifier = Modifier.testTag("attachment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // View Once Toggle Button '①'
                    IconButton(
                        onClick = { isViewOnceActive = !isViewOnceActive },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isViewOnceActive) KachehriGreenPrimary else Color.Transparent)
                            .testTag("view_once_toggle_button")
                    ) {
                        Text(
                            text = "①",
                            color = if (isViewOnceActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Text Input
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Encrypted message...", fontSize = 14.sp) },
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDark) Color(0xFF202C33) else Color.White,
                            unfocusedContainerColor = if (isDark) Color(0xFF202C33) else Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim(), isViewOnceActive, "NONE", null, 0)
                                inputText = ""
                                isViewOnceActive = false
                            }
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .testTag("message_input_field")
                    )

                    // Send or Mic button
                    val isReadyToSend = inputText.isNotBlank() || isViewOnceActive
                    IconButton(
                        onClick = {
                            if (isReadyToSend) {
                                val text = if (inputText.isNotBlank()) inputText.trim() else "Confidential View-Once Note"
                                onSendMessage(text, isViewOnceActive, "NONE", null, 0)
                                inputText = ""
                                isViewOnceActive = false
                            } else {
                                // Simulate sending voice note
                                onSendMessage("Voice message (0:08)", false, "VOICE", null, 8)
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(KachehriGreenPrimary)
                            .testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = if (isReadyToSend) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(chatBg)
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
            ) {
                item {
                    E2EEBanner(onClick = onVerifySafetyNumberClick)
                }

                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        currentTimeMillis = currentTimeMillis,
                        onViewOnceClick = onViewOnceClick,
                        onInspectSecurityClick = onInspectSecurityClick
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // Attachment Sheet
        if (showAttachmentSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAttachmentSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Encrypted Attachments",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Send View Once Confidential Photo
                        AttachmentItem(
                            icon = Icons.Default.Lock,
                            label = "View Once",
                            bg = DisappearingFire,
                            onClick = {
                                showAttachmentSheet = false
                                onSendMessage(
                                    "Confidential schematic",
                                    true,
                                    "PHOTO",
                                    "confidential_sketch",
                                    0
                                )
                            }
                        )

                        // Send Scenic Disappearing Photo
                        AttachmentItem(
                            icon = Icons.Default.Image,
                            label = "Photo",
                            bg = Color(0xFF1E88E5),
                            onClick = {
                                showAttachmentSheet = false
                                onSendMessage(
                                    "Scenic mountain view",
                                    false,
                                    "PHOTO",
                                    "disappearing_sample_photo",
                                    0
                                )
                            }
                        )

                        // Send Encrypted Voice Note
                        AttachmentItem(
                            icon = Icons.Default.Mic,
                            label = "Voice Note",
                            bg = KachehriGreenPrimary,
                            onClick = {
                                showAttachmentSheet = false
                                onSendMessage(
                                    "Voice message (0:14)",
                                    false,
                                    "VOICE",
                                    null,
                                    14
                                )
                            }
                        )

                        // Disappearing Mode Shortcut
                        AttachmentItem(
                            icon = Icons.Default.Timer,
                            label = "Timer",
                            bg = Color(0xFFE65100),
                            onClick = {
                                showAttachmentSheet = false
                                onDisappearingSettingsClick()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun AttachmentItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bg: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
