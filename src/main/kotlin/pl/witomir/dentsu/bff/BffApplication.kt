package pl.witomir.dentsu.bff

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO
import pl.witomir.dentsu.bff.config.FeignConfig

@SpringBootApplication
@EnableCaching
@EnableFeignClients(defaultConfiguration = [FeignConfig::class])
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
class BffApplication

fun main(args: Array<String>) {
	runApplication<BffApplication>(*args)
}
