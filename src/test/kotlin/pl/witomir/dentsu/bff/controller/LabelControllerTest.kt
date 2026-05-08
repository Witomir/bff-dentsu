package pl.witomir.dentsu.bff.controller

import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pl.witomir.dentsu.bff.TestCacheConfig
import pl.witomir.dentsu.bff.exception.GlobalExceptionHandler
import pl.witomir.dentsu.bff.model.response.LabelsResponse
import pl.witomir.dentsu.bff.service.LabelService
import java.util.Locale

@WebMvcTest(LabelController::class)
@Import(GlobalExceptionHandler::class, TestCacheConfig::class)
class LabelControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var labelService: LabelService

    @Test
    fun `GET labels with locale=en returns English labels`() {
        `when`(labelService.getLabels(Locale.ENGLISH)).thenReturn(
            LabelsResponse(
                headline = "Product Search",
                searchPlaceholder = "Search products",
                resultsLabel = "results",
                notFoundMessage = "RESULTS NOT FOUND"
            )
        )

        mockMvc.perform(get("/labels").param("locale", "en"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.headline").value("Product Search"))
            .andExpect(jsonPath("$.searchPlaceholder").value("Search products"))
            .andExpect(jsonPath("$.resultsLabel").value("results"))
            .andExpect(jsonPath("$.notFoundMessage").value("RESULTS NOT FOUND"))
    }

    @Test
    fun `GET labels with locale=de returns German labels`() {
        `when`(labelService.getLabels(Locale.GERMAN)).thenReturn(
            LabelsResponse(
                headline = "Produktsuche",
                searchPlaceholder = "Produkte suchen",
                resultsLabel = "Ergebnisse",
                notFoundMessage = "KEINE ERGEBNISSE GEFUNDEN"
            )
        )

        mockMvc.perform(get("/labels").param("locale", "de"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.headline").value("Produktsuche"))
            .andExpect(jsonPath("$.notFoundMessage").value("KEINE ERGEBNISSE GEFUNDEN"))
    }

    @Test
    fun `GET labels without locale param passes null to service`() {
        `when`(labelService.getLabels(null)).thenReturn(
            LabelsResponse(
                headline = "Product Search",
                searchPlaceholder = "Search products",
                resultsLabel = "results",
                notFoundMessage = "RESULTS NOT FOUND"
            )
        )

        mockMvc.perform(get("/labels"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.headline").value("Product Search"))
    }
}
