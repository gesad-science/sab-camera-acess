package helpers

class ApiConfigManager {
    var apiUrl: String = ""
    var modelName: String = ""

    fun setConfig(
        url: String,
        model: String,
    ) {
        apiUrl = url.trimEnd('/')
        modelName = model
    }

    fun getFinalUrl(): String {
        check(apiUrl.isNotEmpty() && modelName.isNotEmpty()) {
            "API URL or model not configured"
        }
        return "$apiUrl/v1/$modelName"
    }
}
