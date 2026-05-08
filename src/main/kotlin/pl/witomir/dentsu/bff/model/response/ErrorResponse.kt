package pl.witomir.dentsu.bff.model.response

data class ErrorResponse(
    val status: Int,
    val message: String
)
