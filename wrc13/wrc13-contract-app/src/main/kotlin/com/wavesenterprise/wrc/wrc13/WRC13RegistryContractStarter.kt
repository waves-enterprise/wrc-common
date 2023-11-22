package com.wavesenterprise.wrc.wrc13

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.sdk.contract.grpc.GrpcJacksonContractDispatcherBuilder
import com.wavesenterprise.wrc.wrc13.impl.WRC13RegistryContractImpl

fun main() {
    val contractDispatcher = GrpcJacksonContractDispatcherBuilder.builder()
        .contractHandlerType(WRC13RegistryContractImpl::class.java)
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
            .build()
    )