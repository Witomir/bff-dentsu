package pl.witomir.dentsu.bff.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import pl.witomir.dentsu.bff.client.CatalogClient
import pl.witomir.dentsu.bff.model.dto.CatalogProduct

@Service
class CachedCatalogService(private val catalogClient: CatalogClient) {

    @Cacheable("products")
    fun getProducts(): List<CatalogProduct> = catalogClient.getProducts()

    fun getProduct(id: String): CatalogProduct? = catalogClient.getProduct(id)
}
