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

package org.ossreviewtoolkit.plugins.scanners.nomos

import java.io.File
import java.time.Instant

import org.apache.logging.log4j.kotlin.logger

import org.ossreviewtoolkit.model.ScanSummary
import org.ossreviewtoolkit.model.ScannerDetails
import org.ossreviewtoolkit.plugins.api.OrtPlugin
import org.ossreviewtoolkit.plugins.api.PluginDescriptor
import org.ossreviewtoolkit.scanner.LocalPathScannerWrapper
import org.ossreviewtoolkit.scanner.ScanContext
import org.ossreviewtoolkit.scanner.ScannerMatcher
import org.ossreviewtoolkit.scanner.ScannerWrapperFactory
import org.ossreviewtoolkit.utils.common.CommandLineTool
import org.ossreviewtoolkit.utils.common.ProcessCapture
import org.ossreviewtoolkit.utils.ort.createOrtTempDir

object NomosCommand : CommandLineTool {
    override fun command(workingDir: File?): String {
        return listOfNotNull(workingDir, "nomos").joinToString(File.separator)
    }

    override fun getVersionRequirement(): RangesList =
        RangesListFactory.create(">=4.0.0") // will need to adjust range as needed

        override fun transformVersion(output: String): String =
        output.lineSequence().firstNotNullOfOrNull {
            it.substringAfter("nomos build version:").substringBefore("r(").trim()
        }.orEmpty()
}

/**
 * A wrapper for [Nomos](https://github.com/fossology/fossology).
 *
 * This plugin integrates FOSSology's Nomos scanner into ORT by calling its CLI
 * and mapping its output to ORT's scan result format.
 */
@OrtPlugin(
    displayName = "Nomos",
    description = "A wrapper for [Nomos](https://github.com/fossology/fossology).",
    factory = ScannerWrapperFactory::class
)
class Nomos(
    override val descriptor: PluginDescriptor = NomosFactory.descriptor,
    private val config: NomosConfig
) : LocalPathScannerWrapper() {
    // TODO: implement required methods

        // Runs the actual scanner process and returns the raw result as a string.
    override fun runScanner(path: File, context: ScanContext): String {
        val resultFile = createOrtTempDir().resolve("nomos-result.txt")  //either use plain text file or json
        val process = runNomos(path, resultFile)

        return with(process) {
            if (isError && stdout.isNotBlank()) logger.debug { stdout }
            if (stderr.isNotBlank()) logger.debug { stderr }

            resultFile.readText().also { resultFile.parentFile.safeDeleteRecursively() }
        }
    }
}

