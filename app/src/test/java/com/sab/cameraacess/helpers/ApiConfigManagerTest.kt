package com.sab.cameraacess.helpers

import android.content.Context
import android.content.SharedPreferences
import api.ApiConfigManager
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

class ApiConfigManagerTest {
    private lateinit var context: Context
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var apiConfigManager: ApiConfigManager

    @Before
    fun setUp() {
        context = mock(Context::class.java)
        sharedPrefs = mock(SharedPreferences::class.java)
        editor = mock(SharedPreferences.Editor::class.java)
        `when`(
            context
                .getSharedPreferences(
                    "api_config",
                    Context.MODE_PRIVATE,
                ),
        ).thenReturn(sharedPrefs)

        `when`(
            sharedPrefs.edit(),
        ).thenReturn(editor)

        `when`(
            editor.putString(
                anyString(),
                anyString(),
            ),
        ).thenReturn(editor)

        apiConfigManager = ApiConfigManager(context)
    }

    @Test
    fun `setConfig will salve url and model correctly`() {
        val url = "http://10.0.2.2:8000"
        val model = "ResNet"

        apiConfigManager.setConfig(url, model)

        verify(editor).putString("api_url", url)
        verify(editor).putString("model_name", model)
        verify(editor).apply()
    }

    @Test
    fun `getFinalUrl will returned url saved`() {
        `when`(sharedPrefs.getString("api_url", "")).thenReturn("http://10.0.2.2:8000")

        val result = apiConfigManager.getFinalUrl()

        assertEquals("http://10.0.2.2:8000", result)
    }

    @Test
    fun `getModelName will returned model saved`() {
        `when`(sharedPrefs.getString("model_name", "")).thenReturn("MobileNet")

        val result = apiConfigManager.getModelName()

        assertEquals("MobileNet", result)
    }
}
