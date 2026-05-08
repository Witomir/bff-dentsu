package pl.witomir.dentsu.bff.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import pl.witomir.dentsu.bff.model.enum.ProductColor
import pl.witomir.dentsu.bff.model.enum.ProductSize
import pl.witomir.dentsu.bff.model.enum.ProductType

@Service
class ProductSearchService(
    private val productFetchService: ProductFetchService,
    private val productSortService: ProductSortService
) {

    fun searchProducts(
        search: String?,
        types: List<ProductType>,
        colors: List<ProductColor>,
        sizes: List<ProductSize>,
        pageable: Pageable
    ): Page<EnrichedProduct> {
        val fetched = productFetchService.fetchProducts(search, types, colors, sizes)
        val sorted = productSortService.sort(fetched, pageable)

        return buildDto(sorted, pageable)
    }

    fun findProduct(id: String): EnrichedProduct? = productFetchService.findProduct(id)

    private fun buildDto(sorted: List<EnrichedProduct>, pageable: Pageable): PageImpl<EnrichedProduct> {
        val total = sorted.size
        val offset = pageable.offset.toInt().coerceAtMost(total)
        val content = sorted.subList(offset, (offset + pageable.pageSize).coerceAtMost(total))

        return PageImpl(content, pageable, total.toLong())
    }
}
