package pl.witomir.dentsu.bff.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import pl.witomir.dentsu.bff.model.dto.CatalogProduct
import java.math.BigDecimal

class ProductSortServiceTest {

    private val service = ProductSortService()

    private fun product(id: String, price: BigDecimal?) = EnrichedProduct(
        CatalogProduct(id, "T", "D", "blouse", "green", "medium", "/img"),
        price
    )

    private val items = listOf(
        product("p-104", BigDecimal("199.0")),
        product("p-101", BigDecimal("59.9")),
        product("p-103", BigDecimal("49.0")),
        product("p-102", BigDecimal("79.0")),
        product("p-105", BigDecimal("29.9"))
    )

    @Test
    fun `sorts by price ascending`() {
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "price"))
        val result = service.sort(items, pageable)
        assertEquals(listOf(BigDecimal("29.9"), BigDecimal("49.0"), BigDecimal("59.9"), BigDecimal("79.0"), BigDecimal("199.0")), result.map { it.price })
    }

    @Test
    fun `sorts by price descending`() {
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "price"))
        val result = service.sort(items, pageable)
        assertEquals(listOf(BigDecimal("199.0"), BigDecimal("79.0"), BigDecimal("59.9"), BigDecimal("49.0"), BigDecimal("29.9")), result.map { it.price })
    }

    @Test
    fun `null prices are always last regardless of sort direction`() {
        val withNulls = items + product("p-null", null)

        val asc = service.sort(withNulls, PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "price")))
        assertEquals("p-null", asc.last().catalog.id)

        val desc = service.sort(withNulls, PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "price")))
        assertEquals("p-null", desc.last().catalog.id)
    }

    @Test
    fun `returns items unsorted when no sort is specified`() {
        val pageable = PageRequest.of(0, 20)
        val result = service.sort(items, pageable)
        assertEquals(items.map { it.catalog.id }, result.map { it.catalog.id })
    }
}
