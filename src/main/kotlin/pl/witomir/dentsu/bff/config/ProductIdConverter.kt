package pl.witomir.dentsu.bff.config

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import pl.witomir.dentsu.bff.model.validation.ProductId

@Component
class ProductIdConverter : Converter<String, ProductId> {
    override fun convert(source: String): ProductId = ProductId(source)
}
