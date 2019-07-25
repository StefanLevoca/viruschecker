package sk.csirt.viruschecker.driver.antivirus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import mu.KotlinLogging
import sk.csirt.viruschecker.driver.config.AntivirusType
import java.io.File

val logger = KotlinLogging.logger { }

interface Antivirus {
    val type: AntivirusType

    suspend fun scanFile(params: FileScanParameters): FileScanResult

    suspend fun scanFileAndClean(params: FileScanParameters)
            : FileScanResult = supervisorScope {
        val result = scanFile(params)
        launch(Dispatchers.IO) {
            params.fileToScan.delete()
        }
        result
    }
}

data class FileScanParameters(
    val fileToScan: File,
    val originalFileName: String = fileToScan.name
)

data class ScanResult(
    val antivirusType: AntivirusType,
    val status: ScanStatusResult,
    val reports: List<AntivirusReportResult>
) {
    constructor(
        antivirusType: AntivirusType,
        reports: List<AntivirusReportResult>
    ) : this(
        antivirusType = antivirusType,
        reports = reports,
        status = reports.maxBy { it.status }?.status ?: ScanStatusResult.NOT_AVAILABLE
    )
}

data class FileScanResult(
    val filename: String,
    val scanReport: ScanResult
)

data class AntivirusReportResult(
    val antivirusName: String,
    val status: ScanStatusResult,
    val malwareDescription: String
)

/**
 * Do not change the order of constants!
 */
enum class ScanStatusResult {
    NOT_AVAILABLE, OK, INFECTED
}
