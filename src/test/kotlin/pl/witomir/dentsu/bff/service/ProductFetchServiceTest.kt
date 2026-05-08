package pl.witomir.dentsu.bff.service

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import pl.witomir.dentsu.bff.client.PricingClient
import pl.witomir.dentsu.bff.model.dto.CatalogProduct
import pl.witomir.dentsu.bff.model.enum.ProductColor
import pl.witomir.dentsu.bff.model.enum.ProductSize
import pl.witomir.dentsu.bff.model.enum.ProductType
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class ProductFetchServiceTest {

    @Mock private lateinit var cachedCatalogService: CachedCatalogService
    @Mock private lateinit var pricingClient: PricingClient

    private lateinit var service: ProductFetchService

    private val sampleProducts = listOf(
        CatalogProduct("p-101", "Green Linen Blouse", "Desc", "blouse", "green",  "medium", "/images/green-blouse.jpg"),
        CatalogProduct("p-102", "Red Denim Jean",     "Desc", "jean",   "red",    "large",  "/images/red-jean.jpg"),
        CatalogProduct("p-103", "Blue Summer Skirt",  "Desc", "skirt",  "blue",   "small",  "/images/blue-skirt.jpg"),
        CatalogProduct("p-104", "Red Leather Jacket", "Desc", "jacket", "red",    "large",  "/images/red-jacket.jpg"),
        CatalogProduct("p-105", "Yellow Silk Scarf",  "Desc", "scarf",  "yellow", "medium", "/images/yellow-scarf.jpg")
    )

    private val samplePrices = mapOf(
        "p-101" to BigDecimal("59.9"),
        "p-102" to BigDecimal("79.0"),
        "p-103" to BigDecimal("49.0"),
        "p-104" to BigDecimal("199.0"),
        "p-105" to BigDecimal("29.9")
    )

    @BeforeEach
    fun setUp() {
        service = ProductFetchService(cachedCatalogService, pricingClient)
    }

    @Test
    fun `filters by a single color`() {
        `when`(cachedCatalogService.getProducts()).thenReturn(sampleProducts)
        `when`(pricingClient.getPricesForIds(any())).thenReturn(samplePrices)

        val result = service.fetchProducts(null, emptyList(), listOf(ProductColor.GREEN), emptyList())

        assertEquals(1, result.size)
        assertEquals("p-101", result.first().catalog.id)
    }

    @Test
    fun `filters by multiple colors using OR operator`() {
        `when`(cachedCatalogService.getProducts()).thenReturn(sampleProducts)
        `when`(pricingClient.getPricesForIds(any())).thenReturn(samplePrices)

        val result = service.fetchProducts(null, emptyList(), listOf(ProductColor.RED, ProductColor.GREEN), emptyList())

        assertEquals(3, result.size)
        assertEquals(setOf("p-101", "p-102", "p-104"), result.map { it.catalog.id }.toSet())
    }

    @Test
    fun `filters by type AND color across 2 groups`() {
        `when`(cachedCatalogService.getProducts()).thenReturn(sampleProducts)
        `when`(pricingClient.getPricesForIds(any())).thenReturn(samplePrices)

        val result = service.fetchProducts(null, listOf(ProductType.JEAN), listOf(ProductColor.RED), emptyList())

        assertEquals(1, result.size)
        assertEquals("p-102", result.first().catalog.id)
    }

    @Test
    fun `filters by search case-insensitive substring`() {
        `when`(cachedCatalogService.getProducts()).thenReturn(sampleProducts)
        `when`(pricingClient.getPricesForIds(any())).thenReturn(samplePrices)

        val result = service.fetchProducts("LiNeN", emptyList(), emptyList(), emptyList())

        assertEquals(1, result.size)
        assertEquals("p-101", result.first().catalog.id)
    }

    @Test
    fun `filters by size`() {
        `when`(cachedCatalogService.getProducts()).thenReturn(sampleProducts)
        `when`(pricingClient.getPricesForIds(any())).thenReturn(samplePrices)

        val result = service.fetchProducts(null, emptyList(), emptyList(), listOf(ProductSize.SMALL))

        assertEquals(1, result.size)
        assertEquals("p-103", result.first().catalog.id)
    }

    @Test
    fun `enriches products with prices`() {
        `when`(cachedCatalogService.getProducts()).thenReturn(listOf(sampleProducts[0]))
        `when`(pricingClient.getPricesForIds(any())).thenReturn(samplePrices)

        val result = service.fetchProducts(null, emptyList(), emptyList(), emptyList())

        assertEquals("p-101", result.first().catalog.id)
        assertEquals(BigDecimal("59.9"), result.first().price)
    }

    @Test
    fun `returns null price when pricing service throws exception`() {
        `when`(cachedCatalogService.getProducts()).thenReturn(sampleProducts)
        `when`(pricingClient.getPricesForIds(any())).thenThrow(RuntimeException("pricing down"))

        val result = service.fetchProducts(null, emptyList(), emptyList(), emptyList())

        assertEquals(5, result.size)
        result.forEach { assertNull(it.price) }
    }
}
