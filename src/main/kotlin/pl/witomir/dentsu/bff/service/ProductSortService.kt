package pl.witomir.dentsu.bff.service

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service


@Service
class ProductSortService {

    companion object {
        const val PRICE_PARAM_NAME = "price"
    }

    fun sort(items: List<EnrichedProduct>, pageable: Pageable): List<EnrichedProduct> {
        val priceOrder = pageable.sort.getOrderFor(PRICE_PARAM_NAME) ?: return items
        return items.sortedWith(comparator(priceOrder))
    }

    private fun comparator(priceOrder: Sort.Order): Comparator<EnrichedProduct> = Comparator { x, y ->
        val priceX = x.price
        val priceY = y.price

        when {
            priceX == null && priceY == null -> 0
            priceX == null -> 1
            priceY == null -> -1
            priceOrder.isDescending -> priceY.compareTo(priceX)
            else -> priceX.compareTo(priceY)
        }
    }
}
