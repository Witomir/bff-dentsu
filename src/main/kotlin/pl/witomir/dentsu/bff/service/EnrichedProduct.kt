package pl.witomir.dentsu.bff.service

import pl.witomir.dentsu.bff.model.dto.CatalogProduct
import java.math.BigDecimal

data class EnrichedProduct(
    val catalog: CatalogProduct,
    val price: BigDecimal?
)
