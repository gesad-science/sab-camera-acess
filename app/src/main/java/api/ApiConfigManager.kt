package api

object ApiConfigManager {
    private const val BASE_URL = "https://nonpossibly-aspish-fletcher.ngrok-free.dev/mock/classify"
    private const val MODEL_NAME = "mock"

    fun getFinalUrl(): String = BASE_URL

    fun getModelName(): String = MODEL_NAME
}
