package pl.witomir.dentsu.bff.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import pl.witomir.dentsu.bff.TestCacheConfig
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FilterController::class)
@Import(TestCacheConfig::class)
class FilterControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GET filters returns all filter categories`() {
        mockMvc.perform(get("/filters"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.type").isArray)
            .andExpect(jsonPath("$.color").isArray)
            .andExpect(jsonPath("$.size").isArray)
    }

    @Test
    fun `GET filters returns expected types`() {
        mockMvc.perform(get("/filters"))
            .andExpect(jsonPath("$.type[?(@ == 'jean')]").exists())
            .andExpect(jsonPath("$.type[?(@ == 'jacket')]").exists())
            .andExpect(jsonPath("$.type[?(@ == 'skirt')]").exists())
            .andExpect(jsonPath("$.type[?(@ == 'blouse')]").exists())
            .andExpect(jsonPath("$.type[?(@ == 'scarf')]").exists())
    }

    @Test
    fun `GET filters returns expected colors`() {
        mockMvc.perform(get("/filters"))
            .andExpect(jsonPath("$.color[?(@ == 'green')]").exists())
            .andExpect(jsonPath("$.color[?(@ == 'red')]").exists())
            .andExpect(jsonPath("$.color[?(@ == 'blue')]").exists())
            .andExpect(jsonPath("$.color[?(@ == 'yellow')]").exists())
    }

    @Test
    fun `GET filters returns expected sizes`() {
        mockMvc.perform(get("/filters"))
            .andExpect(jsonPath("$.size[?(@ == 'small')]").exists())
            .andExpect(jsonPath("$.size[?(@ == 'medium')]").exists())
            .andExpect(jsonPath("$.size[?(@ == 'large')]").exists())
    }
}
