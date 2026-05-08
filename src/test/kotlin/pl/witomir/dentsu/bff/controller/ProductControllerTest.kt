package pl.witomir.dentsu.bff.controller

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.isNull
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pl.witomir.dentsu.bff.TestCacheConfig
import pl.witomir.dentsu.bff.exception.GlobalExceptionHandler
import pl.witomir.dentsu.bff.model.dto.WebProductDto
import java.math.BigDecimal
import pl.witomir.dentsu.bff.service.ProductService

@WebMvcTest(ProductController::class, ProductSearchController::class)
@Import(GlobalExceptionHandler::class, TestCacheConfig::class)
@EnableSpringDataWebSupport
class ProductControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var productService: ProductService

    private val sampleProduct = WebProductDto(
        id = "p-101", title = "Green Linen Blouse", description = "Desc",
        type = "blouse", color = "green", size = "medium",
        imageUrl = "/images/green-blouse.jpg", price = BigDecimal("59.9")
    )

    @Test
    fun `GET results returns 200 with page structure`() {
        `when`(productService.getWebResults(isNull(), any(), any(), any(), any()))
            .thenReturn(PageImpl(listOf(sampleProduct), PageRequest.of(0, 20), 1))

        mockMvc.perform(get("/results").header("X-Client-Type", "web"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value("p-101"))
            .andExpect(jsonPath("$.page.totalElements").value(1))
            .andExpect(jsonPath("$.page.number").value(0))
            .andExpect(jsonPath("$.page.size").value(20))
    }

    @Test
    fun `GET results returns 200 for mobile with compact payload`() {
        `when`(productService.getMobileResults(isNull(), any(), any(), any(), any()))
            .thenReturn(PageImpl(emptyList(), PageRequest.of(0, 20), 0))

        mockMvc.perform(get("/results").header("X-Client-Type", "mobile"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(0))
    }

    @Test
    fun `GET products id returns 404 when service returns null`() {
        `when`(productService.getWebProduct("p-999")).thenReturn(null)

        mockMvc.perform(get("/products/p-999").header("X-Client-Type", "web"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `GET products id returns 200 with product data`() {
        `when`(productService.getWebProduct("p-101")).thenReturn(sampleProduct)

        mockMvc.perform(get("/products/p-101").header("X-Client-Type", "web"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("p-101"))
            .andExpect(jsonPath("$.title").value("Green Linen Blouse"))
            .andExpect(jsonPath("$.price").value(BigDecimal("59.9")))
    }

    @Test
    fun `GET products with invalid id format returns 400`() {
        mockMvc.perform(get("/products/!!!invalid").header("X-Client-Type", "web"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET results without header returns 404`() {
        mockMvc.perform(get("/results"))
            .andExpect(status().isNotFound)
    }
}
