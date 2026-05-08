package pl.witomir.dentsu.bff.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pl.witomir.dentsu.bff.client.PricingClient
import pl.witomir.dentsu.bff.model.dto.CatalogProduct
import pl.witomir.dentsu.bff.model.enum.ProductColor
import pl.witomir.dentsu.bff.model.enum.ProductSize
import pl.witomir.dentsu.bff.model.enum.ProductType
import java.math.BigDecimal

@Service
class ProductFetchService(
    private val cachedCatalogService: CachedCatalogService,
    private val pricingClient: PricingClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun fetchProducts(
        search: String?,
        types: List<ProductType>,
        colors: List<ProductColor>,
        sizes: List<ProductSize>
    ): List<EnrichedProduct> {
        val filtered = cachedCatalogService.getProducts()
            .filter { matchesSearch(it, search) }
            .filter { matchesGroup(it.type, types) }
            .filter { matchesGroup(it.color, colors) }
            .filter { matchesGroup(it.size, sizes) }

        val prices = fetchPricesFor(filtered.map { it.id })
        return filtered.map { EnrichedProduct(it, prices[it.id]) }
    }

    fun findProduct(id: String): EnrichedProduct? {
        val (product, prices) = runBlocking {
            coroutineScope {
                val productDeferred = async(Dispatchers.IO) { cachedCatalogService.getProduct(id) }
                val pricesDeferred = async(Dispatchers.IO) {
                    runCatching { pricingClient.getPricesForIds(listOf(id)) }.getOrElse { ex ->
                        log.warn("Pricing service unavailable for product {}: {}", id, ex.message)
                        emptyMap()
                    }
                }
                productDeferred.await() to pricesDeferred.await()
            }
        }
        product ?: return null
        return EnrichedProduct(product, prices[product.id])
    }

    private fun fetchPricesFor(ids: List<String>): Map<String, BigDecimal> {
        if (ids.isEmpty()) return emptyMap()
        return runCatching { pricingClient.getPricesForIds(ids) }.getOrElse { ex ->
            log.warn("Pricing service unavailable, returning products without prices: {}", ex.message)
            emptyMap()
        }
    }

    private fun matchesSearch(product: CatalogProduct, searchString: String?): Boolean =
        searchString.isNullOrBlank() || product.title.contains(searchString, ignoreCase = true)

    private fun matchesGroup(value: String, filter: List<Enum<*>>): Boolean =
        filter.isEmpty() || filter.any { it.name.equals(value, ignoreCase = true) }
}
