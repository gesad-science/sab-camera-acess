package helpers

class ApiConfigManager {
    var apiUrl: String = "https://2022f5a5ac59.ngrok-free.app"
    var modelName: String = "ResNet"

    fun setConfig() {
        apiUrl = "https://2022f5a5ac59.ngrok-free.app"
        modelName = "ResNet"
    }

    fun getFinalUrl(): String {
        check(apiUrl.isNotEmpty() && modelName.isNotEmpty()) {
            "API URL or model not configured"
        }
        return "$apiUrl/v1/$modelName"
    }
}
