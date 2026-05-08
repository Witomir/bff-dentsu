package pl.witomir.dentsu.bff.controller

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pl.witomir.dentsu.bff.model.dto.MobileProductDto
import pl.witomir.dentsu.bff.model.enum.ProductColor
import pl.witomir.dentsu.bff.model.enum.ProductSize
import pl.witomir.dentsu.bff.model.enum.ProductType
import pl.witomir.dentsu.bff.model.dto.WebProductDto
import pl.witomir.dentsu.bff.service.ProductService

@RestController
@RequestMapping("/results")
class ProductSearchController(private val productService: ProductService) {

    @GetMapping(headers = ["X-Client-Type=web"])
    fun getResultsWeb(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) type: List<ProductType>?,
        @RequestParam(required = false) color: List<ProductColor>?,
        @RequestParam(required = false) size: List<ProductSize>?,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<WebProductDto> =
        productService.getWebResults(search, type ?: emptyList(), color ?: emptyList(), size ?: emptyList(), pageable)

    @GetMapping(headers = ["X-Client-Type=mobile"])
    fun getResultsMobile(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) type: List<ProductType>?,
        @RequestParam(required = false) color: List<ProductColor>?,
        @RequestParam(required = false) size: List<ProductSize>?,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<MobileProductDto> =
        productService.getMobileResults(search, type ?: emptyList(), color ?: emptyList(), size ?: emptyList(), pageable)
}
