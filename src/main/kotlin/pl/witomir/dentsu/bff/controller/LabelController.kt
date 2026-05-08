package pl.witomir.dentsu.bff.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pl.witomir.dentsu.bff.model.response.LabelsResponse
import pl.witomir.dentsu.bff.service.LabelService
import java.util.Locale

@RestController
@RequestMapping("/labels")
class LabelController(private val labelService: LabelService) {

    @GetMapping
    fun getLabels(@RequestParam(required = false) locale: Locale?): LabelsResponse =
        labelService.getLabels(locale)
}
