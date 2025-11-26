package api

import android.content.Context

class ApiConfigManager(
    private val context: Context,
) {
    private val prefs =
        context.getSharedPreferences(
            "api_config",
            Context.MODE_PRIVATE,
        )

    fun setConfig(
        url: String,
        model: String,
    ) {
        val finalUrl = "$url/v1/$model"
        prefs.edit().apply {
            putString("api_url", finalUrl)
            putString("model_name", model)
            apply()
        }
    }

    fun getFinalUrl(): String = prefs.getString("api_url", "") ?: ""

    fun getModelName(): String = prefs.getString("model_name", "") ?: ""
}
