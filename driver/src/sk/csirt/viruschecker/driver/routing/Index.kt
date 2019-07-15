package sk.csirt.viruschecker.driver.routing

import io.ktor.routing.Route
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import mu.KotlinLogging
import sk.csirt.viruschecker.driver.antivirus.Antivirus
import sk.csirt.viruschecker.routing.payload.AntivirusDriverInfoResponse
import sk.csirt.viruschecker.routing.DriverRoutes

private val logger = KotlinLogging.logger { }


private const val ideRunMessage = "Version not available. Driver application is probably running from IDE."

@KtorExperimentalLocationsAPI
fun Route.index(virusChecker: Antivirus) {
    get<DriverRoutes.Index> {
        call.respond(AntivirusDriverInfoResponse(
            antivirus = virusChecker.type.commonName,
            driverVersion = Antivirus::class.java.`package`.implementationVersion
                ?: ideRunMessage
        ))
    }
}

