package pl.witomir.dentsu.bff.model.validation

@JvmInline
value class ProductId(val value: String) {
    init {
        require(value.matches(PATTERN)) { "Invalid product ID format: '$value'" }
    }

    companion object {
        private val PATTERN = Regex("[a-zA-Z]+-\\d+")
    }
}
