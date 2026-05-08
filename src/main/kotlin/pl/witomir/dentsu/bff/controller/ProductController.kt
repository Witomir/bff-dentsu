package pl.witomir.dentsu.bff.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.witomir.dentsu.bff.model.dto.MobileProductDto
import pl.witomir.dentsu.bff.model.validation.ProductId
import pl.witomir.dentsu.bff.model.dto.WebProductDto
import pl.witomir.dentsu.bff.service.ProductService

@RestController
@RequestMapping("/products")
class ProductController(private val productService: ProductService) {

    @GetMapping("/{id}", headers = ["X-Client-Type=web"])
    fun getProductWeb(@PathVariable id: ProductId): ResponseEntity<WebProductDto> {
        val product = productService.getWebProduct(id.value)
        return if (product != null) ResponseEntity.ok(product) else ResponseEntity.notFound().build()
    }

    @GetMapping("/{id}", headers = ["X-Client-Type=mobile"])
    fun getProductMobile(@PathVariable id: ProductId): ResponseEntity<MobileProductDto> {
        val product = productService.getMobileProduct(id.value)
        return if (product != null) ResponseEntity.ok(product) else ResponseEntity.notFound().build()
    }
}
