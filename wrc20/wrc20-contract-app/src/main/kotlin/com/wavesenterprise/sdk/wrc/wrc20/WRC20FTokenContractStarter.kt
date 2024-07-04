package com.wavesenterprise.sdk.wrc.wrc20

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.sdk.contract.grpc.GrpcJacksonContractDispatcherBuilder
import com.wavesenterprise.sdk.wrc.wrc20.impl.WRC20FTokenContractImpl

fun main() {
    val contractDispatcher = GrpcJacksonContractDispatcherBuilder.builder()
        .contractHandlerType(WRC20FTokenContractImpl::class.java)
        .objectMapper(objectMapper)
        .build()
    contractDispatcher.dispatch()
}

private val objectMapper = jacksonObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .registerModule(JavaTimeModule())
    .registerModule(
        KotlinModule.Builder()
            .configure(KotlinFeature.NullIsSameAsDefault, true)
            .build(),
    )
