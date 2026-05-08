package pl.witomir.dentsu.bff.config

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import pl.witomir.dentsu.bff.model.enum.ProductType

@Component
class ProductTypeConverter : Converter<String, ProductType> {
    override fun convert(source: String): ProductType =
        ProductType.entries.firstOrNull { it.name.equals(source.trim(), ignoreCase = true) }
            ?: throw IllegalArgumentException("Unknown product type: '$source'")
}
