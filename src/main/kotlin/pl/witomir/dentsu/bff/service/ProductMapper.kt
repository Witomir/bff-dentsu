package pl.witomir.dentsu.bff.service

import org.springframework.stereotype.Component
import pl.witomir.dentsu.bff.model.dto.MobileProductDto
import pl.witomir.dentsu.bff.model.dto.WebProductDto

@Component
class ProductMapper {

    fun toWebDto(enriched: EnrichedProduct) = WebProductDto(
        id = enriched.catalog.id,
        title = enriched.catalog.title,
        description = enriched.catalog.description,
        type = enriched.catalog.type,
        color = enriched.catalog.color,
        size = enriched.catalog.size,
        imageUrl = enriched.catalog.imageUrl,
        price = enriched.price
    )

    fun toMobileDto(enriched: EnrichedProduct) = MobileProductDto(
        id = enriched.catalog.id,
        title = enriched.catalog.title,
        thumbnailUrl = enriched.catalog.imageUrl,
        price = enriched.price
    )
}
