package pl.witomir.dentsu.bff.service

import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import pl.witomir.dentsu.bff.model.response.LabelsResponse
import java.util.Locale

@Service
class LabelService(private val messageSource: MessageSource) {

    fun getLabels(locale: Locale?): LabelsResponse {
        val resolvedLocale = locale ?: Locale.ENGLISH
        fun msg(key: String) = messageSource.getMessage(key, null, resolvedLocale)

        return LabelsResponse(
            headline = msg("labels.headline"),
            searchPlaceholder = msg("labels.searchPlaceholder"),
            resultsLabel = msg("labels.resultsLabel"),
            notFoundMessage = msg("labels.notFoundMessage")
        )
    }
}
