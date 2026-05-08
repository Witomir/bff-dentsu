package pl.witomir.dentsu.bff.config

import feign.codec.Decoder
import feign.codec.Encoder
import feign.optionals.OptionalDecoder
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder
import org.springframework.cloud.openfeign.support.SpringDecoder
import org.springframework.cloud.openfeign.support.SpringEncoder
import org.springframework.context.annotation.Bean

class FeignConfig {

    @Bean
    fun feignHttpMessageConverters(
        customizers: ObjectProvider<ClientHttpMessageConvertersCustomizer>,
        cloudCustomizers: ObjectProvider<HttpMessageConverterCustomizer>
    ): FeignHttpMessageConverters = FeignHttpMessageConverters(customizers, cloudCustomizers)

    @Bean
    fun feignDecoder(messageConverters: ObjectProvider<FeignHttpMessageConverters>): Decoder =
        OptionalDecoder(ResponseEntityDecoder(SpringDecoder(messageConverters)))

    @Bean
    fun feignEncoder(messageConverters: ObjectProvider<FeignHttpMessageConverters>): Encoder =
        SpringEncoder(messageConverters)
}
