package sk.csirt.viruschecker.routing.payload

import java.io.File

data class MultiScanRequest(
    val fileToScan: File,
    val originalFilename: String,
    val useExternalDrivers: Boolean
)