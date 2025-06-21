/*
 * Copyright (C) 2025 The ORT Project Authors (see <https://github.com/oss-review-toolkit/ort/blob/main/NOTICE>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package org.ossreviewtoolkit.plugins.scanners.fossologynomossa

import java.time.Instant

import org.ossreviewtoolkit.model.LicenseFinding
import org.ossreviewtoolkit.model.ScanSummary
import org.ossreviewtoolkit.model.TextLocation
import org.ossreviewtoolkit.utils.spdx.SpdxConstants
import org.ossreviewtoolkit.utils.spdx.SpdxExpression
import org.ossreviewtoolkit.utils.spdx.SpdxLicenseIdExpression
import org.ossreviewtoolkit.utils.spdx.toExpression

internal fun NomossaResult.toScanSummary(startTime: Instant, endTime: Instant): ScanSummary {
    val licenseFindings = results.flatMap { fileResult ->
        fileResult.licenses.map { rawLicense ->
            val licenseExpression = runCatching { SpdxExpression.parse(rawLicense) }.getOrNull()

            val safeLicense = when {
                licenseExpression == null -> SpdxConstants.NOASSERTION
                licenseExpression.isValid() -> rawLicense
                else -> "LicenseRef-Nomossa-${rawLicense.replace(Regex("[^A-Za-z0-9.+-]"), "-")}"
            }

            LicenseFinding(
                license = safeLicense,
                location = TextLocation(
                    path = fileResult.file,
                    startLine = TextLocation.UNKNOWN_LINE,
                    endLine = TextLocation.UNKNOWN_LINE
                )
            )
        }
    }.toSet()

    return ScanSummary(
        startTime = startTime,
        endTime = endTime,
        licenseFindings = licenseFindings,
        issues = emptyList(), // no SPDX parsing issues anymore
        copyrightFindings = sortedSetOf()
    )
}
