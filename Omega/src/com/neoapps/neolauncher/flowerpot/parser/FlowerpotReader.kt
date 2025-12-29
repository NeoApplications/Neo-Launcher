package com.neoapps.neolauncher.flowerpot.parser

import com.neoapps.neolauncher.flowerpot.Flowerpot
import com.neoapps.neolauncher.flowerpot.FlowerpotFormatException
import com.neoapps.neolauncher.flowerpot.rules.Rule
import com.neoapps.neolauncher.util.listWhileNotNull
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class FlowerpotReader(inputStream: InputStream) : BufferedReader(InputStreamReader(inputStream)) {
    private var version: Int? = null

    /**
     * Read the next rule from the stream
     * @return the parsed rule or null if the end of the file has been reached
     */
    fun readRule(): Rule? {
        val line = readLine() ?: return null
        val filter = LineParser.parse(line, version)
        if (filter is Rule.Version) {
            if (version != null) {
                throw FlowerpotFormatException("Version declaration can only appear once")
            }
            if (!Flowerpot.SUPPORTED_VERSIONS.contains(filter.version)) {
                throw FlowerpotFormatException("Unsupported version ${filter.version} (supported are ${Flowerpot.SUPPORTED_VERSIONS.joinToString()})")
            }
            version = filter.version
        }
        return filter
    }

    /**
     * Read all rules contained in the file with None and Version rules already filtered out
     */
    fun readRules(): List<Rule> =
        listWhileNotNull { readRule() }.filterNot { it is Rule.None || it is Rule.Version }
}