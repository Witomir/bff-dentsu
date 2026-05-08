package pl.witomir.dentsu.bff.model.dto

data class CatalogProduct(
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val color: String,
    val size: String,
    val imageUrl: String
)
