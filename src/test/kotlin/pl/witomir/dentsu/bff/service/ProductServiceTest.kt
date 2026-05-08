package pl.witomir.dentsu.bff.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import pl.witomir.dentsu.bff.model.dto.CatalogProduct
import pl.witomir.dentsu.bff.model.dto.MobileProductDto
import pl.witomir.dentsu.bff.model.dto.WebProductDto
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class ProductServiceTest {

    @Mock private lateinit var productSearchService: ProductSearchService

    private val productMapper = ProductMapper()
    private lateinit var productService: ProductService

    private val catalog = CatalogProduct("p-101", "Green Linen Blouse", "A blouse", "blouse", "green", "medium", "/images/green-blouse.jpg")
    private val enriched = EnrichedProduct(catalog, BigDecimal("59.9"))
    private val enrichedNoPrice = EnrichedProduct(catalog, null)

    @BeforeEach
    fun setUp() {
        productService = ProductService(productSearchService, productMapper)
    }

    @Test
    fun `getWebResults maps to WebProductDto with all fields`() {
        `when`(productSearchService.searchProducts(null, emptyList(), emptyList(), emptyList(), PageRequest.of(0, 20)))
            .thenReturn(PageImpl(listOf(enriched)))

        val result = productService.getWebResults(null, emptyList(), emptyList(), emptyList(), PageRequest.of(0, 20))
        val dto = result.content.first()

        assertEquals("p-101", dto.id)
        assertEquals("Green Linen Blouse", dto.title)
        assertEquals("A blouse", dto.description)
        assertEquals("blouse", dto.type)
        assertEquals("green", dto.color)
        assertEquals("medium", dto.size)
        assertEquals("/images/green-blouse.jpg", dto.imageUrl)
        assertEquals(BigDecimal("59.9"), dto.price)
    }

    @Test
    fun `getMobileResults maps to MobileProductDto with compact fields`() {
        `when`(productSearchService.searchProducts(null, emptyList(), emptyList(), emptyList(), PageRequest.of(0, 20)))
            .thenReturn(PageImpl(listOf(enriched)))

        val result = productService.getMobileResults(null, emptyList(), emptyList(), emptyList(), PageRequest.of(0, 20))
        val dto = result.content.first()

        assertEquals("p-101", dto.id)
        assertEquals("Green Linen Blouse", dto.title)
        assertEquals("/images/green-blouse.jpg", dto.thumbnailUrl)
        assertEquals(BigDecimal("59.9"), dto.price)
    }

    @Test
    fun `getWebProduct returns null when product not found`() {
        `when`(productSearchService.findProduct("p-999")).thenReturn(null)

        assertNull(productService.getWebProduct("p-999"))
    }

    @Test
    fun `getMobileProduct maps with null price when unavailable`() {
        `when`(productSearchService.findProduct("p-101")).thenReturn(enrichedNoPrice)

        val dto = productService.getMobileProduct("p-101") as MobileProductDto
        assertNull(dto.price)
    }

    @Test
    fun `getWebProduct maps with null price when unavailable`() {
        `when`(productSearchService.findProduct("p-101")).thenReturn(enrichedNoPrice)

        val dto = productService.getWebProduct("p-101") as WebProductDto
        assertNull(dto.price)
    }
}
