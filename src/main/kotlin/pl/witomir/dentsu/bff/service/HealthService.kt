package pl.witomir.dentsu.bff.service

import org.springframework.stereotype.Service
import pl.witomir.dentsu.bff.client.CatalogClient
import pl.witomir.dentsu.bff.client.PricingClient
import pl.witomir.dentsu.bff.model.response.HealthResponse
import pl.witomir.dentsu.bff.model.enum.OverallStatus
import pl.witomir.dentsu.bff.model.enum.ServiceStatus

@Service
class HealthService(
    private val catalogClient: CatalogClient,
    private val pricingClient: PricingClient
) {
    fun check(): HealthResponse {
        val services = mapOf(
            "catalog" to probe { catalogClient.getProduct("p-101") },
            "pricing" to probe { pricingClient.getPricesForIds(listOf("p-101")) }
        )
        val overall = when {
            services.values.all { it == ServiceStatus.UP } -> OverallStatus.UP
            services.values.all { it == ServiceStatus.DOWN } -> OverallStatus.DOWN
            else -> OverallStatus.DEGRADED
        }
        return HealthResponse(status = overall, services = services)
    }

    private fun probe(call: () -> Any?): ServiceStatus =
        runCatching { call(); ServiceStatus.UP }
            .getOrElse { ServiceStatus.DOWN }
}
