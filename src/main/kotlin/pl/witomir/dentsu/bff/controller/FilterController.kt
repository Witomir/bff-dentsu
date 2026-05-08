package pl.witomir.dentsu.bff.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.witomir.dentsu.bff.model.response.FiltersResponse
import pl.witomir.dentsu.bff.model.enum.ProductColor
import pl.witomir.dentsu.bff.model.enum.ProductSize
import pl.witomir.dentsu.bff.model.enum.ProductType

@RestController
@RequestMapping("/filters")
class FilterController {

    @GetMapping
    fun getFilters(): FiltersResponse = FiltersResponse(
        type = ProductType.entries.map { it.name.lowercase() },
        color = ProductColor.entries.map { it.name.lowercase() },
        size = ProductSize.entries.map { it.name.lowercase() }
    )
}
