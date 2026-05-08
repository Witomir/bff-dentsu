package pl.witomir.dentsu.bff.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import pl.witomir.dentsu.bff.model.dto.MobileProductDto
import pl.witomir.dentsu.bff.model.dto.WebProductDto
import pl.witomir.dentsu.bff.model.enum.ProductColor
import pl.witomir.dentsu.bff.model.enum.ProductSize
import pl.witomir.dentsu.bff.model.enum.ProductType

@Service
class ProductService(
    private val productSearchService: ProductSearchService,
    private val productMapper: ProductMapper
) {

    fun getWebResults(
        search: String?,
        types: List<ProductType>,
        colors: List<ProductColor>,
        sizes: List<ProductSize>,
        pageable: Pageable
    ): Page<WebProductDto> =
        productSearchService
            .searchProducts(search, types, colors, sizes, pageable)
            .map { productMapper.toWebDto(it) }

    fun getMobileResults(
        search: String?,
        types: List<ProductType>,
        colors: List<ProductColor>,
        sizes: List<ProductSize>,
        pageable: Pageable
    ): Page<MobileProductDto> =
        productSearchService
            .searchProducts(search, types, colors, sizes, pageable)
            .map { productMapper.toMobileDto(it) }

    fun getWebProduct(id: String): WebProductDto? =
        productSearchService
            .findProduct(id)
            ?.let { productMapper.toWebDto(it) }

    fun getMobileProduct(id: String): MobileProductDto? =
        productSearchService
            .findProduct(id)
            ?.let { productMapper.toMobileDto(it) }
}
