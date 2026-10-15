package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.crypto.E2EEEngine
import com.example.data.db.KachehriDatabase
import com.example.data.model.CallLogEntity
import com.example.data.model.ChatWithContact
import com.example.data.model.ContactEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusUpdateEntity
import com.example.data.repository.KachehriRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.data.model.AuthUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

enum class MainTab {
    CHATS, STATUS, CALLS, PRIVACY
}

enum class ChatFilter {
    ALL, UNREAD, ENCRYPTED, GROUPS
}

class KachehriViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KachehriRepository
    private var tickerJob: Job? = null
    private var realtimeSyncJob: Job? = null

    // Firebase Auth session
    private val firebaseAuth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    private val _currentUser = MutableStateFlow<AuthUser?>(
        firebaseAuth?.currentUser?.let { AuthUser.fromFirebaseUser(it) }
    )
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        if (user != null) {
            _currentUser.value = AuthUser.fromFirebaseUser(user)
        }
    }

    init {
        val db = KachehriDatabase.getDatabase(application)
        repository = KachehriRepository(db.kachehriDao())
        startDisappearingMessageTicker()
        firebaseAuth?.addAuthStateListener(authStateListener)
    }

    fun onUserSignedIn(user: AuthUser) {
        _currentUser.value = user
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            // ignore
        }
        _currentUser.value = null
    }

    // Cloud Firestore synchronization state
    val syncState = repository.firestoreRepo.syncState
    val isFirestoreAvailable: Boolean = repository.firestoreRepo.isAvailable()

    // Navigation & Tabs
    private val _currentTab = MutableStateFlow(MainTab.CHATS)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _selectedChatId = MutableStateFlow<String?>(null)
    val selectedChatId: StateFlow<String?> = _selectedChatId.asStateFlow()

    private val _selectedContactId = MutableStateFlow<String?>(null)
    val selectedContactId: StateFlow<String?> = _selectedContactId.asStateFlow()

    private val _chatFilter = MutableStateFlow(ChatFilter.ALL)
    val chatFilter: StateFlow<ChatFilter> = _chatFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Modals and viewers
    private val _viewOnceMessage = MutableStateFlow<MessageEntity?>(null)
    val viewOnceMessage: StateFlow<MessageEntity?> = _viewOnceMessage.asStateFlow()

    private val _securityInspectorMessage = MutableStateFlow<MessageEntity?>(null)
    val securityInspectorMessage: StateFlow<MessageEntity?> = _securityInspectorMessage.asStateFlow()

    private val _showSafetyNumber = MutableStateFlow(false)
    val showSafetyNumber: StateFlow<Boolean> = _showSafetyNumber.asStateFlow()

    private val _showDisappearingSettings = MutableStateFlow(false)
    val showDisappearingSettings: StateFlow<Boolean> = _showDisappearingSettings.asStateFlow()

    private val _activeCallContact = MutableStateFlow<ContactEntity?>(null)
    val activeCallContact: StateFlow<ContactEntity?> = _activeCallContact.asStateFlow()
    private val _isCallVideo = MutableStateFlow(false)
    val isCallVideo: StateFlow<Boolean> = _isCallVideo.asStateFlow()

    private val _showNewChatDialog = MutableStateFlow(false)
    val showNewChatDialog: StateFlow<Boolean> = _showNewChatDialog.asStateFlow()

    private val _viewingStatus = MutableStateFlow<StatusUpdateEntity?>(null)
    val viewingStatus: StateFlow<StatusUpdateEntity?> = _viewingStatus.asStateFlow()

    private val _showAddStatusDialog = MutableStateFlow(false)
    val showAddStatusDialog: StateFlow<Boolean> = _showAddStatusDialog.asStateFlow()

    // Current ticker time to refresh countdown UI
    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    // Data streams
    val allChats: StateFlow<List<ChatWithContact>> = repository.getAllChats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeContact: StateFlow<ContactEntity?> = _selectedContactId
        .flatMapLatest { id ->
            if (id != null) repository.getContact(id) else flowOf(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentMessages: StateFlow<List<MessageEntity>> = _selectedChatId
        .flatMapLatest { chatId ->
            if (chatId != null) repository.getMessagesForChat(chatId) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statusUpdates: StateFlow<List<StatusUpdateEntity>> = repository.getActiveStatusUpdates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callLogs: StateFlow<List<CallLogEntity>> = repository.getAllCalls()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun setChatFilter(filter: ChatFilter) {
        _chatFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openChat(chatId: String, contactId: String) {
        _selectedChatId.value = chatId
        _selectedContactId.value = contactId
        viewModelScope.launch {
            repository.markChatAsRead(chatId)
        }
        realtimeSyncJob?.cancel()
        realtimeSyncJob = repository.startRealtimeSync(chatId, viewModelScope)
    }

    fun closeChat() {
        realtimeSyncJob?.cancel()
        realtimeSyncJob = null
        _selectedChatId.value = null
        _selectedContactId.value = null
    }

    fun sendMessage(
        text: String,
        isViewOnce: Boolean = false,
        mediaType: String = "NONE",
        mediaResName: String? = null,
        voiceDurationSeconds: Int = 0
    ) {
        val chatId = _selectedChatId.value ?: return
        val contact = activeContact.value ?: return
        val disappearingSeconds = contact.disappearingTimerSeconds

        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                contactId = contact.id,
                text = text,
                isViewOnce = isViewOnce,
                mediaType = mediaType,
                mediaResName = mediaResName,
                voiceDurationSeconds = voiceDurationSeconds,
                disappearingSeconds = disappearingSeconds
            )
        }
    }

    fun openViewOnce(message: MessageEntity) {
        if (!message.isOpened) {
            _viewOnceMessage.value = message
        }
    }

    fun closeViewOnce() {
        val msg = _viewOnceMessage.value
        if (msg != null) {
            viewModelScope.launch {
                repository.markViewOnceOpened(msg.id, msg.chatId)
            }
        }
        _viewOnceMessage.value = null
    }

    fun openSecurityInspector(message: MessageEntity) {
        _securityInspectorMessage.value = message
    }

    fun closeSecurityInspector() {
        _securityInspectorMessage.value = null
    }

    fun openSafetyNumber() {
        _showSafetyNumber.value = true
    }

    fun closeSafetyNumber() {
        _showSafetyNumber.value = false
    }

    fun toggleContactVerified() {
        val contact = activeContact.value ?: return
        viewModelScope.launch {
            repository.setContactVerified(contact.id, !contact.isVerified)
        }
    }

    fun openDisappearingSettings() {
        _showDisappearingSettings.value = true
    }

    fun closeDisappearingSettings() {
        _showDisappearingSettings.value = false
    }

    fun setDisappearingTimer(seconds: Int) {
        val contact = activeContact.value ?: return
        viewModelScope.launch {
            repository.setChatDisappearingTimer(contact.id, seconds)
            val desc = when (seconds) {
                0 -> "Disappearing messages turned off."
                10 -> "⏱️ Disappearing messages set to 10 seconds (Live Demo mode)."
                86400 -> "⏱️ Disappearing messages set to 24 hours."
                604800 -> "⏱️ Disappearing messages set to 7 days."
                else -> "⏱️ Disappearing messages timer updated."
            }
            // Send notice message
            val chatId = _selectedChatId.value ?: return@launch
            repository.sendMessage(
                chatId = chatId,
                contactId = contact.id,
                text = desc,
                disappearingSeconds = 0
            )
        }
        _showDisappearingSettings.value = false
    }

    fun clearChat() {
        val chatId = _selectedChatId.value ?: return
        viewModelScope.launch {
            repository.clearChat(chatId)
        }
    }

    fun startCall(contact: ContactEntity, isVideo: Boolean) {
        _activeCallContact.value = contact
        _isCallVideo.value = isVideo
    }

    fun endCall(durationSeconds: Int = 0) {
        val contact = _activeCallContact.value
        if (contact != null) {
            viewModelScope.launch {
                repository.logCall(
                    contactId = contact.id,
                    contactName = contact.name,
                    contactAvatarColor = contact.avatarColor,
                    isVideo = _isCallVideo.value,
                    isOutgoing = true,
                    duration = durationSeconds
                )
            }
        }
        _activeCallContact.value = null
    }

    fun showNewChat() {
        _showNewChatDialog.value = true
    }

    fun hideNewChat() {
        _showNewChatDialog.value = false
    }

    fun createNewChat(name: String, phone: String) {
        viewModelScope.launch {
            val contactId = "c_" + UUID.randomUUID().toString().take(6)
            val colors = listOf(0xFF00796B, 0xFF8E24AA, 0xFF1E88E5, 0xFFD81B60, 0xFFE65100, 0xFF43A047)
            val color = colors.random()
            val contact = ContactEntity(
                id = contactId,
                name = name,
                phone = phone,
                avatarColor = color,
                statusText = "Hey there! I am using Kachehri.",
                safetyNumber = E2EEEngine.generateSafetyNumber(contactId),
                isVerified = false,
                isOnline = true,
                lastSeen = "online"
            )
            val chatId = repository.createNewChat(contact)
            _showNewChatDialog.value = false
            openChat(chatId, contactId)
        }
    }

    fun viewStatus(status: StatusUpdateEntity) {
        _viewingStatus.value = status
    }

    fun closeStatusViewer() {
        _viewingStatus.value = null
    }

    fun showAddStatus() {
        _showAddStatusDialog.value = true
    }

    fun hideAddStatus() {
        _showAddStatusDialog.value = false
    }

    fun addStatus(text: String, imageResName: String? = null, bgColor: Long = 0xFF00796B) {
        viewModelScope.launch {
            repository.postStatus(text, imageResName, bgColor)
            _showAddStatusDialog.value = false
        }
    }

    private fun startDisappearingMessageTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(1000)
                _currentTimeMillis.value = System.currentTimeMillis()
                repository.burnExpiredMessages()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        realtimeSyncJob?.cancel()
        firebaseAuth?.removeAuthStateListener(authStateListener)
    }
}
