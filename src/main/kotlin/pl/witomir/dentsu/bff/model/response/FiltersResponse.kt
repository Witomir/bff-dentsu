package pl.witomir.dentsu.bff.model.response

data class FiltersResponse(
    val type: List<String>,
    val color: List<String>,
    val size: List<String>
)
