package pl.witomir.dentsu.bff.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.math.BigDecimal

@FeignClient(name = "pricing", url = "\${services.pricing.url}")
interface PricingClient {

    @GetMapping("/prices")
    fun getPricesForIds(@RequestParam ids: List<String>): Map<String, BigDecimal>
}
