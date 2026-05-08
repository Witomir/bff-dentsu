package pl.witomir.dentsu.bff.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.witomir.dentsu.bff.model.response.HealthResponse
import pl.witomir.dentsu.bff.model.enum.OverallStatus
import pl.witomir.dentsu.bff.service.HealthService

@RestController
@RequestMapping("/health")
class HealthController(private val healthService: HealthService) {

    @GetMapping
    fun health(): ResponseEntity<HealthResponse> {
        val response = healthService.check()
        val status = if (response.status == OverallStatus.DOWN) HttpStatus.SERVICE_UNAVAILABLE else HttpStatus.OK
        return ResponseEntity.status(status).body(response)
    }
}
