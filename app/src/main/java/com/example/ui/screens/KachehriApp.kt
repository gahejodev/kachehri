package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddStatusDialog
import com.example.ui.components.DisappearingSettingsDialog
import com.example.ui.components.EncryptedCallScreen
import com.example.ui.components.NewChatDialog
import com.example.ui.components.SafetyNumberScreen
import com.example.ui.components.SecurityInspectorDialog
import com.example.ui.components.StatusStoryViewer
import com.example.ui.components.ViewOnceViewer
import com.example.ui.viewmodel.KachehriViewModel

@Composable
fun KachehriApp(
    viewModel: KachehriViewModel = viewModel()
) {
    val selectedChatId by viewModel.selectedChatId.collectAsStateWithLifecycle()
    val activeContact by viewModel.activeContact.collectAsStateWithLifecycle()
    val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val currentTimeMillis by viewModel.currentTimeMillis.collectAsStateWithLifecycle()

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val chats by viewModel.allChats.collectAsStateWithLifecycle()
    val statusUpdates by viewModel.statusUpdates.collectAsStateWithLifecycle()
    val callLogs by viewModel.callLogs.collectAsStateWithLifecycle()
    val chatFilter by viewModel.chatFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    // Modals
    val viewOnceMessage by viewModel.viewOnceMessage.collectAsStateWithLifecycle()
    val securityInspectorMessage by viewModel.securityInspectorMessage.collectAsStateWithLifecycle()
    val showSafetyNumber by viewModel.showSafetyNumber.collectAsStateWithLifecycle()
    val showDisappearingSettings by viewModel.showDisappearingSettings.collectAsStateWithLifecycle()
    val activeCallContact by viewModel.activeCallContact.collectAsStateWithLifecycle()
    val isCallVideo by viewModel.isCallVideo.collectAsStateWithLifecycle()
    val showNewChatDialog by viewModel.showNewChatDialog.collectAsStateWithLifecycle()
    val viewingStatus by viewModel.viewingStatus.collectAsStateWithLifecycle()
    val showAddStatusDialog by viewModel.showAddStatusDialog.collectAsStateWithLifecycle()

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    if (currentUser == null) {
        LoginScreen(
            onSignInSuccess = { user ->
                viewModel.onUserSignedIn(user)
            }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedChatId != null && activeContact != null) {
            BackHandler {
                if (showSafetyNumber) {
                    viewModel.closeSafetyNumber()
                } else {
                    viewModel.closeChat()
                }
            }

            if (showSafetyNumber) {
                SafetyNumberScreen(
                    contact = activeContact!!,
                    onBackClick = { viewModel.closeSafetyNumber() },
                    onToggleVerified = { viewModel.toggleContactVerified() }
                )
            } else {
                ChatScreen(
                    contact = activeContact!!,
                    messages = currentMessages,
                    currentTimeMillis = currentTimeMillis,
                    onBackClick = { viewModel.closeChat() },
                    onSendMessage = { text, isViewOnce, mediaType, mediaResName, voiceSec ->
                        viewModel.sendMessage(text, isViewOnce, mediaType, mediaResName, voiceSec)
                    },
                    onViewOnceClick = { viewModel.openViewOnce(it) },
                    onInspectSecurityClick = { viewModel.openSecurityInspector(it) },
                    onVerifySafetyNumberClick = { viewModel.openSafetyNumber() },
                    onDisappearingSettingsClick = { viewModel.openDisappearingSettings() },
                    onStartCallClick = { isVideo ->
                        viewModel.startCall(activeContact!!, isVideo)
                    },
                    onClearChatClick = { viewModel.clearChat() }
                )
            }
        } else {
            MainScreen(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) },
                chats = chats,
                statusUpdates = statusUpdates,
                callLogs = callLogs,
                chatFilter = chatFilter,
                onFilterSelected = { viewModel.setChatFilter(it) },
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onOpenChat = { chatId, contactId ->
                    viewModel.openChat(chatId, contactId)
                },
                onViewStatus = { viewModel.viewStatus(it) },
                onStartCall = { contact, isVideo ->
                    viewModel.startCall(contact, isVideo)
                },
                onShowNewChat = { viewModel.showNewChat() },
                onShowAddStatus = { viewModel.showAddStatus() },
                currentUser = currentUser,
                onSignOut = { viewModel.signOut() }
            )
        }

        // Overlay Modals
        viewOnceMessage?.let { msg ->
            ViewOnceViewer(
                message = msg,
                onCloseAndBurn = { viewModel.closeViewOnce() }
            )
        }

        securityInspectorMessage?.let { msg ->
            SecurityInspectorDialog(
                message = msg,
                onDismiss = { viewModel.closeSecurityInspector() }
            )
        }

        if (showDisappearingSettings && activeContact != null) {
            DisappearingSettingsDialog(
                currentTimerSeconds = activeContact!!.disappearingTimerSeconds,
                contactName = activeContact!!.name,
                onDismiss = { viewModel.closeDisappearingSettings() },
                onSaveTimer = { seconds ->
                    viewModel.setDisappearingTimer(seconds)
                }
            )
        }

        activeCallContact?.let { contact ->
            EncryptedCallScreen(
                contact = contact,
                isVideo = isCallVideo,
                onEndCall = { duration ->
                    viewModel.endCall(duration)
                }
            )
        }

        if (showNewChatDialog) {
            NewChatDialog(
                onDismiss = { viewModel.hideNewChat() },
                onCreateChat = { name, phone ->
                    viewModel.createNewChat(name, phone)
                }
            )
        }

        viewingStatus?.let { status ->
            StatusStoryViewer(
                status = status,
                onDismiss = { viewModel.closeStatusViewer() }
            )
        }

        if (showAddStatusDialog) {
            AddStatusDialog(
                onDismiss = { viewModel.hideAddStatus() },
                onPostStatus = { text, imageResName, bgColor ->
                    viewModel.addStatus(text, imageResName, bgColor)
                }
            )
        }
    }
}
