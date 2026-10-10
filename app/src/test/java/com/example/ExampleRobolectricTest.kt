package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.GeminiManager
import com.example.domain.generator.LegalDraftGenerator
import com.example.domain.models.LegalDraftData
import com.example.domain.models.LegalDraftType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    assertEquals("CyberSahara AI", appName)
  }

  @Test
  fun `verify legal draft generation`() {
    val data = LegalDraftData(
      draftType = LegalDraftType.VAKALATNAMA,
      petitioner = "रामेश्वर प्रसाद",
      advocateName = "अनुराग श्रीवास्तव"
    )
    val text = LegalDraftGenerator.generateText(data)
    assertTrue(text.contains("वकालतनामा"))
    assertTrue(text.contains("रामेश्वर प्रसाद"))
    assertTrue(text.contains("अनुराग श्रीवास्तव"))
  }

  @Test
  fun `verify gemini api key security and validation protocol`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = GeminiManager(context)

    // Key must start with AIzaSy
    val invalidKeySaved = manager.saveApiKey("wrong_key_12345678901234567890")
    assertFalse(invalidKeySaved)

    // Valid key starting with AIzaSy
    val validSample = "AIzaSyDummySecretKeyForTestExecution123456"
    val validKeySaved = manager.saveApiKey(validSample)
    assertTrue(validKeySaved)
    assertTrue(manager.hasValidKey())

    // Zero-Exposure: masked key must never leak the full secret
    val masked = manager.getMaskedKey()
    assertTrue(masked.contains("••••"))
    assertFalse(masked == validSample)

    // Clear key resets state
    manager.clearApiKey()
    assertFalse(manager.hasValidKey())
  }
}
