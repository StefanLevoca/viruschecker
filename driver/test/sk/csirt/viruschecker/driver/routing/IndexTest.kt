package sk.csirt.viruschecker.driver.routing

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.handleRequest
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import sk.csirt.viruschecker.routing.DriverRoutes
import sk.csirt.viruschecker.routing.payload.DriverInfoResponse
import sk.csirt.viruschecker.utils.fromJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
internal class IndexTest : RoutingTest() {
    @Test
    fun `Index test`() {
        createTestApplication {
            handleRequest(HttpMethod.Get, DriverRoutes.index).apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(response.content?.fromJson<DriverInfoResponse>())
            }
        }
    }
}