package pl.witomir.dentsu.bff.model.response

data class LabelsResponse(
    val headline: String,
    val searchPlaceholder: String,
    val resultsLabel: String,
    val notFoundMessage: String
)
