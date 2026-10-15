package com.example.data.db

import com.example.data.crypto.E2EEEngine
import com.example.data.model.CallLogEntity
import com.example.data.model.ChatEntity
import com.example.data.model.ContactEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusUpdateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object DatabaseSeeder {

    suspend fun seedInitialData(dao: KachehriDao) = withContext(Dispatchers.IO) {
        // Contacts
        val contact1 = ContactEntity(
            id = "c_zaid",
            name = "سارنگ سنڌي",
            phone = "+92 300 1234567",
            avatarColor = 0xFF00796B,
            statusText = "جيئي سنڌ، جيئي سنڌي ٻولي 🌸",
            safetyNumber = E2EEEngine.generateSafetyNumber("c_zaid"),
            isVerified = true,
            isOnline = true,
            lastSeen = "آن لائن",
            disappearingTimerSeconds = 0
        )

        val contact2 = ContactEntity(
            id = "c_fatima",
            name = "مارئي سومرو",
            phone = "+92 321 9876543",
            avatarColor = 0xFF8E24AA,
            statusText = "سنڌ جي سونهن ۽ پنهنجپڻو • ڳجهي ڪچهري 🛡️",
            safetyNumber = E2EEEngine.generateSafetyNumber("c_fatima"),
            isVerified = false,
            isOnline = false,
            lastSeen = "15 منٽ اڳ",
            disappearingTimerSeconds = 86400 // 24 hours
        )

        val contact3 = ContactEntity(
            id = "c_tariq",
            name = "سچل لطيفي",
            phone = "+92 333 4567890",
            avatarColor = 0xFF1E88E5,
            statusText = "محبت سندو نينهن نئون، راز سدا محفوظ 📜",
            safetyNumber = E2EEEngine.generateSafetyNumber("c_tariq"),
            isVerified = true,
            isOnline = true,
            lastSeen = "آن لائن",
            disappearingTimerSeconds = 0
        )

        val contact4 = ContactEntity(
            id = "c_ayla",
            name = "بختاور سنڌي",
            phone = "+92 345 6789012",
            avatarColor = 0xFFD81B60,
            statusText = "سنڌي ٻولي، ثقافت ۽ اجرڪ جا دلڪش رنگ 🌹",
            safetyNumber = E2EEEngine.generateSafetyNumber("c_ayla"),
            isVerified = false,
            isOnline = false,
            lastSeen = "اڄ صبح 11:20 تي",
            disappearingTimerSeconds = 0
        )

        val contact5 = ContactEntity(
            id = "c_security_group",
            name = "سنڌ سنگت ڪچهري گروپ 🔒",
            phone = "سنڌي ثقافتي گروپ • 18 رڪن",
            avatarColor = 0xFFE65100,
            statusText = "سنڌي ٻولي ۽ ثقافتي رابطا",
            safetyNumber = E2EEEngine.generateSafetyNumber("c_security_group"),
            isVerified = true,
            isOnline = true,
            lastSeen = "18 رڪن",
            disappearingTimerSeconds = 0
        )

        dao.insertContact(contact1)
        dao.insertContact(contact2)
        dao.insertContact(contact3)
        dao.insertContact(contact4)
        dao.insertContact(contact5)

        // Chats
        val chat1 = ChatEntity(
            id = "chat_zaid",
            contactId = "c_zaid",
            unreadCount = 1,
            isPinned = true,
            lastMessageText = "① هڪ ڀيرو ڏسو (ثقافتي تصوير)",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 3
        )

        val chat2 = ChatEntity(
            id = "chat_fatima",
            contactId = "c_fatima",
            unreadCount = 0,
            isPinned = false,
            lastMessageText = "غائب ٿيندڙ پيغام لڳل: 24 ڪلاڪن بعد خود مٽجي ويندا",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 45
        )

        val chat3 = ChatEntity(
            id = "chat_tariq",
            contactId = "c_tariq",
            unreadCount = 0,
            isPinned = false,
            lastMessageText = "▶ آواز جو پيغام (0:18)",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 180
        )

        val chat4 = ChatEntity(
            id = "chat_ayla",
            contactId = "c_ayla",
            unreadCount = 2,
            isPinned = false,
            lastMessageText = "سنڌوءَ جي ڪناري شام جو دلڪش منظر هتي ڏسو",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 360
        )

        val chat5 = ChatEntity(
            id = "chat_security_group",
            contactId = "c_security_group",
            unreadCount = 0,
            isPinned = false,
            lastMessageText = "ڪچهريءَ ۾ انڪرپشن جا سڀ رازداراڻا ڪوڊ تصديق ٿيل آهن.",
            lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 600
        )

        dao.insertChat(chat1)
        dao.insertChat(chat2)
        dao.insertChat(chat3)
        dao.insertChat(chat4)
        dao.insertChat(chat5)

        // Messages for Sarang Sindhi
        val now = System.currentTimeMillis()
        val enc1 = E2EEEngine.encrypt("اسلام عليڪم سائين! ڪچهري ايپ ۾ ڀلي ڪري آيا. هتي هر پيغام مڪمل رازداراڻو ۽ پڄاڻيءَ تائين محفوظ آهي.", "chat_zaid")
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "chat_zaid",
                senderId = "c_zaid",
                text = "اسلام عليڪم سائين! ڪچهري ايپ ۾ ڀلي ڪري آيا. هتي هر پيغام مڪمل رازداراڻو ۽ پڄاڻيءَ تائين محفوظ آهي.",
                cipherTextHex = enc1.cipherTextHex,
                ivHex = enc1.ivHex,
                timestamp = now - 1000 * 60 * 15,
                isOutgoing = false,
                status = "READ"
            )
        )

        val enc2 = E2EEEngine.encrypt("واهه سائين واهه! غائب ٿيندڙ تصوير ۽ وڊيو ڪيئن ڪم ڪندي آهي؟", "chat_zaid")
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "chat_zaid",
                senderId = "me",
                text = "واهه سائين واهه! غائب ٿيندڙ تصوير ۽ وڊيو ڪيئن ڪم ڪندي آهي؟",
                cipherTextHex = enc2.cipherTextHex,
                ivHex = enc2.ivHex,
                timestamp = now - 1000 * 60 * 10,
                isOutgoing = true,
                status = "READ"
            )
        )

        val enc3 = E2EEEngine.encrypt("هيڏانهن ڏسو، هي هڪ ڀيرو ڏسڻ واري تصوير آهي. هڪ دفعو کولڻ ۽ ڏسڻ کان پوءِ سدائين لاءِ مٽجي ويندي!", "chat_zaid")
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "chat_zaid",
                senderId = "c_zaid",
                text = "هيڏانهن ڏسو، هي هڪ ڀيرو ڏسڻ واري تصوير آهي. هڪ دفعو کولڻ ۽ ڏسڻ کان پوءِ سدائين لاءِ مٽجي ويندي!",
                cipherTextHex = enc3.cipherTextHex,
                ivHex = enc3.ivHex,
                timestamp = now - 1000 * 60 * 5,
                isOutgoing = false,
                status = "READ"
            )
        )

        // View Once confidential sketch
        val enc4 = E2EEEngine.encrypt("[انڪرپٽ ٿيل تصوير: confidential_sketch]", "chat_zaid")
        dao.insertMessage(
            MessageEntity(
                id = "msg_view_once_zaid",
                chatId = "chat_zaid",
                senderId = "c_zaid",
                text = "خفيه سنڌي اجرڪ نقشو ۽ ڊزائين",
                cipherTextHex = enc4.cipherTextHex,
                ivHex = enc4.ivHex,
                timestamp = now - 1000 * 60 * 3,
                isOutgoing = false,
                status = "READ",
                isViewOnce = true,
                isOpened = false,
                mediaType = "PHOTO",
                mediaResName = "confidential_sketch"
            )
        )

        // Messages for Marvi Soomro
        val encFatima1 = E2EEEngine.encrypt("توهان غائب ٿيندڙ پيغام لڳائي ڇڏيا آهن. نوان پيغام 24 ڪلاڪن بعد خودبخود مٽجي ويندا.", "chat_fatima")
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "chat_fatima",
                senderId = "me",
                text = "غائب ٿيندڙ پيغامن جو ٽائمر لڳايو ويو (24 ڪلاڪ).",
                cipherTextHex = encFatima1.cipherTextHex,
                ivHex = encFatima1.ivHex,
                timestamp = now - 1000 * 60 * 50,
                isOutgoing = true,
                status = "READ",
                disappearingSeconds = 86400,
                expiresAt = now + 86400000L - (1000 * 60 * 50)
            )
        )

        val encFatima2 = E2EEEngine.encrypt("واهه بهترين! مون به پنهنجي موبائيل مان حفاظتي ڪوڊ تصديق ڪري ورتو آهي. هاڻي اسان جي ڪچهري مڪمل محفوظ آهي.", "chat_fatima")
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "chat_fatima",
                senderId = "c_fatima",
                text = "واهه بهترين! مون به پنهنجي موبائيل مان حفاظتي ڪوڊ تصديق ڪري ورتو آهي. هاڻي اسان جي ڪچهري مڪمل محفوظ آهي.",
                cipherTextHex = encFatima2.cipherTextHex,
                ivHex = encFatima2.ivHex,
                timestamp = now - 1000 * 60 * 45,
                isOutgoing = false,
                status = "READ",
                disappearingSeconds = 86400,
                expiresAt = now + 86400000L - (1000 * 60 * 45)
            )
        )

        // Messages for Sachal Latifi
        val encTariq1 = E2EEEngine.encrypt("آواز جو پيغام (0:18)", "chat_tariq")
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "chat_tariq",
                senderId = "c_tariq",
                text = "آواز جو پيغام: شاهه لطيف جي وائي ۽ سنڌي ڪچهري",
                cipherTextHex = encTariq1.cipherTextHex,
                ivHex = encTariq1.ivHex,
                timestamp = now - 1000 * 60 * 180,
                isOutgoing = false,
                status = "READ",
                mediaType = "VOICE",
                voiceDurationSeconds = 18
            )
        )

        // Messages for Bakhtawar
        val encAyla = E2EEEngine.encrypt("سنڌوءَ جي ڪناري شام جو دلڪش نظارو", "chat_ayla")
        dao.insertMessage(
            MessageEntity(
                id = "msg_view_once_ayla",
                chatId = "chat_ayla",
                senderId = "c_ayla",
                text = "سنڌوءَ جي لهرن ۽ ٻيڙين جو منظر",
                cipherTextHex = encAyla.cipherTextHex,
                ivHex = encAyla.ivHex,
                timestamp = now - 1000 * 60 * 360,
                isOutgoing = false,
                status = "READ",
                isViewOnce = true,
                isOpened = false,
                mediaType = "PHOTO",
                mediaResName = "disappearing_sample_photo"
            )
        )

        // Seed Status stories
        dao.insertStatusUpdate(
            StatusUpdateEntity(
                id = "status_fatima",
                contactId = "c_fatima",
                userName = "مارئي سومرو",
                avatarColor = 0xFF8E24AA,
                timestamp = now - 1000 * 60 * 120,
                text = "اڄ ڪراچي پريس ڪلب ۾ سنڌي ثقافتي ڏهاڙي جي ملهائجڻ واري تقريب 🌸",
                backgroundColor = 0xFF7C0A02,
                isViewed = false
            )
        )

        dao.insertStatusUpdate(
            StatusUpdateEntity(
                id = "status_zaid",
                contactId = "c_zaid",
                userName = "سارنگ سنڌي",
                avatarColor = 0xFF00796B,
                timestamp = now - 1000 * 60 * 240,
                text = "ڀٽ شاهه درگاهه جي سونهن ۽ لطيف سائين جو امن وارو پيغام 🕊️",
                imageResName = "disappearing_sample_photo",
                backgroundColor = 0xFF0B141A,
                isViewed = false
            )
        )

        // Seed Call logs
        dao.insertCallLog(
            CallLogEntity(
                id = "call_1",
                contactId = "c_zaid",
                contactName = "سارنگ سنڌي",
                contactAvatarColor = 0xFF00796B,
                timestamp = now - 1000 * 60 * 60 * 2,
                isVideo = false,
                isOutgoing = false,
                isMissed = false,
                durationSeconds = 480,
                e2eeVerified = true
            )
        )

        dao.insertCallLog(
            CallLogEntity(
                id = "call_2",
                contactId = "c_fatima",
                contactName = "مارئي سومرو",
                contactAvatarColor = 0xFF8E24AA,
                timestamp = now - 1000 * 60 * 60 * 5,
                isVideo = true,
                isOutgoing = true,
                isMissed = false,
                durationSeconds = 194,
                e2eeVerified = true
            )
        )

        dao.insertCallLog(
            CallLogEntity(
                id = "call_3",
                contactId = "c_tariq",
                contactName = "سچل لطيفي",
                contactAvatarColor = 0xFF1E88E5,
                timestamp = now - 1000 * 60 * 60 * 26,
                isVideo = false,
                isOutgoing = false,
                isMissed = true,
                durationSeconds = 0,
                e2eeVerified = true
            )
        )
    }
}
