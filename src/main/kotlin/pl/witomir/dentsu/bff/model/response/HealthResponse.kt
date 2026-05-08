package pl.witomir.dentsu.bff.model.response

import pl.witomir.dentsu.bff.model.enum.OverallStatus
import pl.witomir.dentsu.bff.model.enum.ServiceStatus

data class HealthResponse(
    val status: OverallStatus,
    val services: Map<String, ServiceStatus>
)
