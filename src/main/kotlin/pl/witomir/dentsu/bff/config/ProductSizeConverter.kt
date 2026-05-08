package pl.witomir.dentsu.bff.config

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import pl.witomir.dentsu.bff.model.enum.ProductSize

@Component
class ProductSizeConverter : Converter<String, ProductSize> {
    override fun convert(source: String): ProductSize =
        ProductSize.entries.firstOrNull { it.name.equals(source.trim(), ignoreCase = true) }
            ?: throw IllegalArgumentException("Unknown product size: '$source'")
}
