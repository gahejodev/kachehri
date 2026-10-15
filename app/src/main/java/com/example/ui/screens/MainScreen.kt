package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import com.example.data.model.AuthUser
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallLogEntity
import com.example.data.model.ChatWithContact
import com.example.data.model.ContactEntity
import com.example.data.model.StatusUpdateEntity
import com.example.ui.theme.DarkBgMain
import com.example.ui.theme.DarkSurfaceHeader
import com.example.ui.theme.DisappearingFire
import com.example.ui.theme.KachehriGreenDark
import com.example.ui.theme.KachehriGreenPrimary
import com.example.ui.viewmodel.ChatFilter
import com.example.ui.viewmodel.MainTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    chats: List<ChatWithContact>,
    statusUpdates: List<StatusUpdateEntity>,
    callLogs: List<CallLogEntity>,
    chatFilter: ChatFilter,
    onFilterSelected: (ChatFilter) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenChat: (chatId: String, contactId: String) -> Unit,
    onViewStatus: (StatusUpdateEntity) -> Unit,
    onStartCall: (contact: ContactEntity, isVideo: Boolean) -> Unit,
    onShowNewChat: () -> Unit,
    onShowAddStatus: () -> Unit,
    currentUser: AuthUser? = null,
    onSignOut: () -> Unit = {}
) {
    var isSearchActive by remember { mutableStateOf(false) }
    val isDark = MaterialTheme.colorScheme.background == DarkBgMain ||
            MaterialTheme.colorScheme.surface == DarkSurfaceHeader

    val headerColor = if (isDark) DarkSurfaceHeader else KachehriGreenDark

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(headerColor)) {
                // Main Header
                if (isSearchActive) {
                    TopAppBar(
                        title = {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                placeholder = { Text("Search encrypted chats...", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_input_field")
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                isSearchActive = false
                                onSearchQueryChange("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close search", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = headerColor)
                    )
                } else {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(KachehriGreenPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Kachehri",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "E2EE & Disappearing Media",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = KachehriGreenPrimary
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchActive = true }, modifier = Modifier.testTag("search_button")) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                            }
                            IconButton(onClick = { onShowNewChat() }, modifier = Modifier.testTag("new_chat_top_button")) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "Add Contact", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = headerColor)
                    )
                }

                // WhatsApp 4-Tab Bar
                TabRow(
                    selectedTabIndex = currentTab.ordinal,
                    containerColor = headerColor,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                            color = KachehriGreenPrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = currentTab == MainTab.CHATS,
                        onClick = { onTabSelected(MainTab.CHATS) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("CHATS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                val unread = chats.sumOf { it.chat.unreadCount }
                                if (unread > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(KachehriGreenPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("$unread", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("tab_chats")
                    )

                    Tab(
                        selected = currentTab == MainTab.STATUS,
                        onClick = { onTabSelected(MainTab.STATUS) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("STATUS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (statusUpdates.any { !it.isViewed }) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(KachehriGreenPrimary)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.testTag("tab_status")
                    )

                    Tab(
                        selected = currentTab == MainTab.CALLS,
                        onClick = { onTabSelected(MainTab.CALLS) },
                        text = { Text("CALLS", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        modifier = Modifier.testTag("tab_calls")
                    )

                    Tab(
                        selected = currentTab == MainTab.PRIVACY,
                        onClick = { onTabSelected(MainTab.PRIVACY) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PRIVACY", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        },
                        modifier = Modifier.testTag("tab_privacy")
                    )
                }
            }
        },
        floatingActionButton = {
            when (currentTab) {
                MainTab.CHATS -> {
                    FloatingActionButton(
                        onClick = onShowNewChat,
                        containerColor = KachehriGreenPrimary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.testTag("main_new_chat_fab")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "New Encrypted Chat")
                    }
                }
                MainTab.STATUS -> {
                    FloatingActionButton(
                        onClick = onShowAddStatus,
                        containerColor = KachehriGreenPrimary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.testTag("main_add_status_fab")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Add Status")
                    }
                }
                MainTab.CALLS -> {
                    FloatingActionButton(
                        onClick = onShowNewChat,
                        containerColor = KachehriGreenPrimary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.testTag("main_new_call_fab")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Start Encrypted Call")
                    }
                }
                MainTab.PRIVACY -> {}
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                MainTab.CHATS -> ChatsTabContent(
                    chats = chats,
                    filter = chatFilter,
                    searchQuery = searchQuery,
                    onFilterSelected = onFilterSelected,
                    onOpenChat = onOpenChat
                )
                MainTab.STATUS -> StatusTabContent(
                    statusUpdates = statusUpdates,
                    onViewStatus = onViewStatus,
                    onAddStatus = onShowAddStatus
                )
                MainTab.CALLS -> CallsTabContent(
                    callLogs = callLogs,
                    onStartCallWithContact = { contactId ->
                        val chat = chats.find { it.contact?.id == contactId }
                        if (chat?.contact != null) {
                            onStartCall(chat.contact, false)
                        }
                    }
                )
                MainTab.PRIVACY -> PrivacyTabContent(currentUser, onSignOut)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatsTabContent(
    chats: List<ChatWithContact>,
    filter: ChatFilter,
    searchQuery: String,
    onFilterSelected: (ChatFilter) -> Unit,
    onOpenChat: (chatId: String, contactId: String) -> Unit
) {
    val filteredChats = remember(chats, filter, searchQuery) {
        chats.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.contact?.name?.contains(searchQuery, ignoreCase = true) == true ||
                    item.chat.lastMessageText.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (filter) {
                ChatFilter.ALL -> true
                ChatFilter.UNREAD -> item.chat.unreadCount > 0
                ChatFilter.ENCRYPTED -> item.contact?.isVerified == true
                ChatFilter.GROUPS -> item.contact?.name?.contains("Group") == true || item.contact?.phone?.contains("members") == true
            }
            matchesSearch && matchesFilter
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChatFilter.entries.forEach { f ->
                val selected = filter == f
                FilterChip(
                    selected = selected,
                    onClick = { onFilterSelected(f) },
                    label = {
                        Text(
                            text = f.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 12.5.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KachehriGreenPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = KachehriGreenPrimary
                    )
                )
            }
        }

        if (filteredChats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No encrypted chats found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredChats, key = { it.chat.id }) { item ->
                    val contact = item.contact
                    if (contact != null) {
                        ChatItemRow(
                            chat = item.chat,
                            contact = contact,
                            onClick = { onOpenChat(item.chat.id, contact.id) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatItemRow(
    chat: com.example.data.model.ChatEntity,
    contact: ContactEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("chat_item_${chat.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with online dot
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(contact.avatarColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            if (contact.isOnline) {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(KachehriGreenPrimary)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Center: Name & Last Message
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = contact.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
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

                Text(
                    text = formatTimestamp(chat.lastMessageTimestamp),
                    fontSize = 12.sp,
                    color = if (chat.unreadCount > 0) KachehriGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Disappearing message clock icon
                    if (contact.disappearingTimerSeconds > 0) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Disappearing message active",
                            tint = DisappearingFire,
                            modifier = Modifier
                                .size(13.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    // View once icon
                    if (chat.lastMessageText.startsWith("①")) {
                        Text(
                            text = "① ",
                            color = KachehriGreenPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = chat.lastMessageText,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chat.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(15.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(KachehriGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${chat.unreadCount}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusTabContent(
    statusUpdates: List<StatusUpdateEntity>,
    onViewStatus: (StatusUpdateEntity) -> Unit,
    onAddStatus: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)) {
        // My Status Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddStatus)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00796B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("M", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(KachehriGreenPrimary)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "My Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap to share an ephemeral 24h update",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "RECENT EPHEMERAL UPDATES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }

        items(statusUpdates, key = { it.id }) { status ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewStatus(status) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("status_item_${status.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Segmented green ring for unseen status
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .border(
                            2.5.dp,
                            if (status.isViewed) Color.Gray.copy(alpha = 0.4f) else KachehriGreenPrimary,
                            CircleShape
                        )
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(Color(status.avatarColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = status.userName.take(1),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = status.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Expires in 24 hours • E2EE",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CallsTabContent(
    callLogs: List<CallLogEntity>,
    onStartCallWithContact: (contactId: String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)) {
        // Header card explaining E2EE calling
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = KachehriGreenPrimary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = KachehriGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Your personal audio and video calls are end-to-end encrypted with safety emoji verification.",
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = "RECENT CALLS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(callLogs, key = { it.id }) { call ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(call.contactAvatarColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = call.contactName.take(1),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = call.contactName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (call.isMissed) Color(0xFFEA0038) else MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when {
                            call.isMissed -> Icons.Default.CallMissed
                            call.isOutgoing -> Icons.AutoMirrored.Filled.CallMade
                            else -> Icons.AutoMirrored.Filled.CallReceived
                        }
                        val tint = when {
                            call.isMissed -> Color(0xFFEA0038)
                            call.isOutgoing -> KachehriGreenPrimary
                            else -> KachehriGreenPrimary
                        }
                        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${formatTimestamp(call.timestamp)} • E2EE Verified",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { onStartCallWithContact(call.contactId) }) {
                    Icon(
                        imageVector = if (call.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = "Call again",
                        tint = KachehriGreenPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyTabContent(
    currentUser: AuthUser? = null,
    onSignOut: () -> Unit = {}
) {
    var appLockEnabled by remember { mutableStateOf(false) }
    var screenshotBlockEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (currentUser != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = KachehriGreenPrimary.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("user_account_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(KachehriGreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                val initial = (currentUser.displayName?.take(1)
                                    ?: currentUser.email?.take(1)
                                    ?: if (currentUser.isAnonymous) "G" else "U").uppercase()
                                Text(
                                    text = initial,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser.displayName ?: currentUser.email ?: "Guest User",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when {
                                        currentUser.isLocalDemo -> "Demo Session • Encrypted Offline"
                                        currentUser.isAnonymous -> "Ephemeral Session • Firebase Auth"
                                        else -> "Authenticated • Firebase & Google"
                                    },
                                    fontSize = 12.sp,
                                    color = KachehriGreenPrimary
                                )
                                Text(
                                    text = "UID: ${currentUser.uid.take(12)}...",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = onSignOut,
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Sign Out",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            // E2EE Protocol Status Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = KachehriGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "End-to-End Cryptography",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Kachehri implements military-grade AES-256-GCM symmetric encryption combined with the Double Ratchet Algorithm and ephemeral session keys. Only you and the person you're communicating with can read your messages or listen to calls.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProtocolBadge("Cipher: AES-256")
                        ProtocolBadge("Ratchet: Curve25519")
                        ProtocolBadge("Auth: SHA-256")
                    }
                }
            }
        }

        item {
            // Disappearing Media Overview Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = DisappearingFire,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Disappearing Media Engine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• View-Once photos destroy their decryption keys as soon as you exit the viewer.\n• Disappearing messages live countdown timer automatically evicts records from the local SQLite Room database.\n• Forwarding and screenshotting are blocked at the display layer.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            // Security Toggles
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = KachehriGreenPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("App Lock / Biometrics", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("Require biometric unlock to open Kachehri", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = appLockEnabled,
                            onCheckedChange = { appLockEnabled = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = KachehriGreenPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = KachehriGreenPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Screenshot Prevention", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("FLAG_SECURE active on View Once media", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = screenshotBlockEnabled,
                            onCheckedChange = { screenshotBlockEnabled = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = KachehriGreenPrimary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProtocolBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = KachehriGreenPrimary.copy(alpha = 0.12f)
    ) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = KachehriGreenPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun formatTimestamp(millis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - millis
    return when {
        diff < 60 * 1000 -> "just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(Date(millis))
        }
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            sdf.format(Date(millis))
        }
    }
}
