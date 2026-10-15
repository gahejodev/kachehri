package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kachehri", appName)
  }

  @Test
  fun `verify E2EE encryption and decryption roundtrip`() {
    val contactId = "contact_test_123"
    val originalSecret = "Top secret meeting at 10 PM. Burn after reading."
    val encrypted = com.example.data.crypto.E2EEEngine.encrypt(originalSecret, contactId)

    org.junit.Assert.assertNotEquals(originalSecret, encrypted.cipherTextHex)
    org.junit.Assert.assertTrue(encrypted.cipherTextHex.isNotEmpty())
    org.junit.Assert.assertTrue(encrypted.ivHex.isNotEmpty())

    val decrypted = com.example.data.crypto.E2EEEngine.decrypt(encrypted.cipherTextHex, encrypted.ivHex, contactId)
    assertEquals(originalSecret, decrypted)
  }

  @Test
  fun `verify safety number format consists of 12 blocks of 5 digits`() {
    val contactId = "contact_test_123"
    val safetyNumber = com.example.data.crypto.E2EEEngine.generateSafetyNumber(contactId)
    val blocks = safetyNumber.split(" ")
    assertEquals(12, blocks.size)
    blocks.forEach { block ->
      assertEquals(5, block.length)
      org.junit.Assert.assertTrue(block.all { it.isDigit() })
    }
  }

  @Test
  fun `verify call verification emojis generates exactly 4 emojis`() {
    val emojis = com.example.data.crypto.E2EEEngine.generateCallSafetyEmojis("contact_test_123")
    assertEquals(4, emojis.size)
  }

  @Test
  fun `verify FirestoreChatRepository initialization and safe offline fallback`() {
    val repo = com.example.data.repository.FirestoreChatRepository(firestore = null)
    org.junit.Assert.assertFalse(repo.isAvailable())
    assertEquals(com.example.data.repository.FirestoreChatRepository.SyncState.Idle, repo.syncState.value)
  }

  @Test
  fun `verify default web client id string resource exists`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val clientId = context.getString(R.string.default_web_client_id)
    org.junit.Assert.assertTrue(clientId.isNotEmpty())
  }
}
