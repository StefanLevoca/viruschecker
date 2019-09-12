package sk.csirt.viruschecker.client.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import sk.csirt.viruschecker.routing.payload.UrlDriverInfoResponse
import sk.csirt.viruschecker.routing.GatewayRoutes

class GatewayInfoService(
    private val gatewayUrl: String,
    private val client: HttpClient
) {
    private val logger = KotlinLogging.logger { }
    suspend fun info(): List<UrlDriverInfoResponse> = coroutineScope {
        client.get<List<UrlDriverInfoResponse>>("$gatewayUrl${GatewayRoutes.driversInfo}")
    }
}