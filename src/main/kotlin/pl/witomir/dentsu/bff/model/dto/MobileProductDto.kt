package pl.witomir.dentsu.bff.model.dto

data class MobileProductDto(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val price: java.math.BigDecimal?
)
