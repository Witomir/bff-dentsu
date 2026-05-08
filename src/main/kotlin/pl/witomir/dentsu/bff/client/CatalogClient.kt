package pl.witomir.dentsu.bff.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import pl.witomir.dentsu.bff.model.dto.CatalogProduct

@FeignClient(name = "catalog", url = "\${services.catalog.url}", dismiss404 = true)
interface CatalogClient {

    @GetMapping("/products")
    fun getProducts(): List<CatalogProduct>

    @GetMapping("/products/{id}")
    fun getProduct(@PathVariable id: String): CatalogProduct?
}
