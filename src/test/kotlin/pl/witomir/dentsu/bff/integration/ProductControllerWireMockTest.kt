package pl.witomir.dentsu.bff.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.cache.CacheManager
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource

/**
 * Integration tests for ProductController using embedded WireMock.
 * No external services required.
 *
 * Covers:
 * 1. Degradation — pricing down → products returned with null prices
 * 2. Recovery — second call after pricing recovers returns full prices
 * 3. Sort defaults — sort=price without direction defaults to ascending
 * 4. Caching — second request serves catalog from cache without hitting downstream
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestPropertySource(properties = [
    "spring.cache.type=caffeine",
    "spring.cache.cache-names=products",
    "spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=5m",
    "spring.cloud.openfeign.circuitbreaker.enabled=false",
    "spring.cloud.openfeign.client.config.catalog.read-timeout=5000",
    "spring.cloud.openfeign.client.config.pricing.read-timeout=1000"
])
class ProductControllerWireMockTest {

    companion object {
        private val catalogServer = WireMockServer(options().dynamicPort())
        private val pricingServer = WireMockServer(options().dynamicPort())

        private val catalogBody = """
            [
              {"id":"p-1","title":"Red Jacket","description":"A red jacket.","type":"jacket","color":"red","size":"medium","imageUrl":"/img/p-1.jpg"},
              {"id":"p-2","title":"Blue Scarf","description":"A blue scarf.","type":"scarf","color":"blue","size":"small","imageUrl":"/img/p-2.jpg"},
              {"id":"p-3","title":"Green Blouse","description":"A green blouse.","type":"blouse","color":"green","size":"large","imageUrl":"/img/p-3.jpg"},
              {"id":"p-4","title":"Yellow Jean","description":"A yellow jean.","type":"jean","color":"yellow","size":"medium","imageUrl":"/img/p-4.jpg"},
              {"id":"p-5","title":"Red Skirt","description":"A red skirt.","type":"skirt","color":"red","size":"small","imageUrl":"/img/p-5.jpg"}
            ]
        """.trimIndent()

        // Prices deliberately out-of-price order to make sort tests meaningful
        private val pricingBody = """{"p-1":199.0,"p-2":29.9,"p-3":59.9,"p-4":89.0,"p-5":49.9}"""

        @JvmStatic
        @BeforeAll
        fun startServers() {
            catalogServer.start()
            pricingServer.start()
        }

        @JvmStatic
        @AfterAll
        fun stopServers() {
            catalogServer.stop()
            pricingServer.stop()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureUrls(registry: DynamicPropertyRegistry) {
            registry.add("services.catalog.url") { "http://localhost:${catalogServer.port()}" }
            registry.add("services.pricing.url") { "http://localhost:${pricingServer.port()}" }
        }
    }

    @Autowired private lateinit var restTemplate: TestRestTemplate
    @Autowired private lateinit var cacheManager: CacheManager

    @BeforeEach
    fun reset() {
        catalogServer.resetAll()
        pricingServer.resetAll()
        cacheManager.getCache("products")?.clear()
    }

    private fun stubCatalog(delayMs: Int = 0) {
        catalogServer.stubFor(
            get(urlPathEqualTo("/products"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(catalogBody)
                        .withFixedDelay(delayMs)
                )
        )
    }

    private fun stubPricingOk() {
        pricingServer.stubFor(
            get(urlPathEqualTo("/prices"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(pricingBody)
                )
        )
    }

    private fun stubPricingDown() {
        pricingServer.stubFor(
            get(urlPathEqualTo("/prices"))
                .willReturn(aResponse().withStatus(500))
        )
    }

    private fun getResults(query: String = ""): Pair<HttpStatus, String> {
        val headers = HttpHeaders().apply { set("X-Client-Type", "web") }
        val response = restTemplate.exchange(
            "/results$query", HttpMethod.GET, HttpEntity<Void>(headers), String::class.java
        )
        return response.statusCode as HttpStatus to (response.body ?: "")
    }

    private fun <T> read(body: String, path: String): T = JsonPath.read(body, path)

    @Test
    fun `returns 200 with null prices for all products when pricing service is down`() {
        stubCatalog()
        stubPricingDown()

        val (status, body) = getResults()

        assertEquals(HttpStatus.OK, status)
        assertEquals(5, read<Int>(body, "$.page.totalElements"))

        val prices: List<Any?> = read(body, "$.content[*].price")
        assertTrue(prices.isNotEmpty())
        assertTrue(prices.all { it == null }, "Expected all prices to be null when pricing is down, got: $prices")
    }

    @Test
    fun `catalog data is still complete in degraded response`() {
        stubCatalog()
        stubPricingDown()

        val (_, body) = getResults()

        val ids: List<String> = read(body, "$.content[*].id")
        val titles: List<String> = read(body, "$.content[*].title")

        assertEquals(setOf("p-1", "p-2", "p-3", "p-4", "p-5"), ids.toSet())
        assertTrue(titles.none { it.isBlank() })
    }

    // ── 2. Recovery after failure ─────────────────────────────────────────────

    @Test
    fun `returns full prices on second call after pricing service recovers`() {
        stubCatalog()
        stubPricingDown()

        // First call — pricing down, all prices null
        val (status1, body1) = getResults()
        assertEquals(HttpStatus.OK, status1)
        val prices1: List<Any?> = read(body1, "$.content[*].price")
        assertTrue(prices1.all { it == null }, "First call: expected all prices null")

        // Pricing recovers
        pricingServer.resetAll()
        stubPricingOk()

        // Second call — catalog served from cache, pricing now works
        val (status2, body2) = getResults()
        assertEquals(HttpStatus.OK, status2)
        val prices2: List<Any?> = read(body2, "$.content[*].price")
        assertTrue(prices2.none { it == null }, "Second call: expected all prices present after recovery")

        // Catalog was only fetched once — second call used the cache
        catalogServer.verify(1, getRequestedFor(urlPathEqualTo("/products")))
    }

    // ── 3. Sort defaults ─────────────────────────────────────────────────────

    @Test
    fun `sort=price without direction defaults to ascending order`() {
        stubCatalog()
        stubPricingOk()

        val (status, body) = getResults("?sort=price")

        assertEquals(HttpStatus.OK, status)

        val prices: List<Double> = read(body, "$.content[*].price")
        assertEquals(prices.sorted(), prices, "Expected prices in ascending order: $prices")
    }

    @Test
    fun `sort=price,asc returns prices in ascending order`() {
        stubCatalog()
        stubPricingOk()

        val (status, body) = getResults("?sort=price,asc")

        assertEquals(HttpStatus.OK, status)

        val prices: List<Double> = read(body, "$.content[*].price")
        assertEquals(prices.sorted(), prices)
    }

    @Test
    fun `unknown sort field returns 200 in catalog order without error`() {
        stubCatalog()
        stubPricingOk()

        val (status, body) = getResults("?sort=unknown_field")

        assertEquals(HttpStatus.OK, status)
        assertEquals(5, read<Int>(body, "$.page.totalElements"))

        val ids: List<String> = read(body, "$.content[*].id")
        assertEquals(listOf("p-1", "p-2", "p-3", "p-4", "p-5"), ids)
    }

    // ── 4. Caching ───────────────────────────────────────────────────────────

    @Test
    fun `second request does not call catalog downstream again`() {
        stubCatalog()
        stubPricingOk()

        getResults()
        getResults()

        catalogServer.verify(1, getRequestedFor(urlPathEqualTo("/products")))
    }

    @Test
    fun `second request is faster than first due to catalog cache`() {
        val catalogDelay = 4000
        stubCatalog(delayMs = catalogDelay)
        stubPricingOk()

        val start1 = System.currentTimeMillis()
        val (status1, _) = getResults()
        val duration1 = System.currentTimeMillis() - start1

        assertEquals(HttpStatus.OK, status1)
        assertTrue(duration1 >= catalogDelay.toLong(),
            "First call should be slow (catalog delay ${catalogDelay}ms), actual: ${duration1}ms")

        val start2 = System.currentTimeMillis()
        val (status2, _) = getResults()
        val duration2 = System.currentTimeMillis() - start2

        assertEquals(HttpStatus.OK, status2)
        assertTrue(duration2 < catalogDelay.toLong(),
            "Second call should be faster than catalog delay (${catalogDelay}ms), actual: ${duration2}ms")

        catalogServer.verify(1, getRequestedFor(urlPathEqualTo("/products")))
    }
}
