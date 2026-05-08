package pl.witomir.dentsu.bff.config

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import pl.witomir.dentsu.bff.model.enum.ProductColor

@Component
class ProductColorConverter : Converter<String, ProductColor> {
    override fun convert(source: String): ProductColor =
        ProductColor.entries.firstOrNull { it.name.equals(source.trim(), ignoreCase = true) }
            ?: throw IllegalArgumentException("Unknown product color: '$source'")
}
