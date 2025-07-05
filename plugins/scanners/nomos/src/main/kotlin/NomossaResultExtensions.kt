package org.ossreviewtoolkit.plugins.scanners.nomos

import java.time.Instant
import org.ossreviewtoolkit.model.LicenseFinding
import org.ossreviewtoolkit.model.ScanSummary
import org.ossreviewtoolkit.model.TextLocation

import org.apache.logging.log4j.kotlin.logger
import org.ossreviewtoolkit.utils.spdx.*

import org.ossreviewtoolkit.model.Issue





fun NomossaResult.toScanSummary(startTime: Instant, endTime: Instant): ScanSummary {
    val licenseFindings = results.flatMap { fileResult ->
        fileResult.licenses.map { rawLicense ->

            val safeLicense = if (rawLicense.matches(Regex("^[A-Za-z0-9.\\-+]+$"))) {
                rawLicense
            } else {
                "LicenseRef-Nomossa-${rawLicense.replace(Regex("[^A-Za-z0-9.+-]"), "-")}"
            }

            LicenseFinding(
                license = safeLicense,  //  use raw string
                location = TextLocation(
                    path = fileResult.file,
                    startLine = 1,
                    endLine = 1
                )
            )
        }
    }.toSortedSet(
        compareBy(
            { it.license.toString() },
            { it.location.path },
            { it.location.startLine }
        )
    )

    return ScanSummary(
        startTime = startTime,
        endTime = endTime,
        licenseFindings = licenseFindings,
        issues = emptyList(), //no SPDX parsing issues anymore
        copyrightFindings = sortedSetOf()
    )
}
