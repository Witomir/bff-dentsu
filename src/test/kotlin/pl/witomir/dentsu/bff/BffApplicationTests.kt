package pl.witomir.dentsu.bff

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(
    properties = [
        "services.catalog.url=http://localhost:9999",
        "services.pricing.url=http://localhost:9998"
    ]
)
class BffApplicationTests {

    @Test
    fun contextLoads() {
    }
}
