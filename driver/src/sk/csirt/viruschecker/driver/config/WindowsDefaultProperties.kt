package sk.csirt.viruschecker.driver.config

import sk.csirt.viruschecker.driver.antivirus.RunProgramCommand
import sk.csirt.viruschecker.driver.config.DriverPropertiesFactory.missingApiKeyPlaceHolder

internal const val defaultPropertiesWindows = """
# Avast
# ==============================================================================

${Properties.avast}=ashCmd.exe ${RunProgramCommand.SCAN_FILE} /_> ${RunProgramCommand.REPORT_FILE}

# Eset
# ==============================================================================

${Properties.eset}=ecls.exe ${RunProgramCommand.SCAN_FILE} /log-all

# Kaspersky
# ==============================================================================

${Properties.kaspersky}=avp.com scan ${RunProgramCommand.SCAN_FILE} /RA /i0

# Microsoft
# ==============================================================================

${Properties.microsoft}=MpCmdRun.exe -Scan -ScanType 3 -File ${RunProgramCommand.SCAN_FILE} -DisableRemediation

# VirusTotal
# ==============================================================================

${Properties.virusTotal}=$missingApiKeyPlaceHolder
"""