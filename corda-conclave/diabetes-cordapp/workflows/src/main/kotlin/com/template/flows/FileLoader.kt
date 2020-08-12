package com.template.flows;

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*

object FileLoader {
    val OBJECT_SEPARATER = "\n"
    val RECORD_SEPARATER = ";"
    val ELEMENT_SEPARATER = ","

    fun loadDataFromResource(file: String): String {
        val inputStream = FileLoader::class.java.getResourceAsStream(file)
        val streamReader = InputStreamReader(inputStream)
        val reader = BufferedReader(streamReader)

        val stringBuilder = StringBuilder()
        var line = ""
        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line).append(RECORD_SEPARATER)
        }
        return stringBuilder.toString()
    }

    fun loadDataFromFile(file: String): String {
        val stringBuilder = StringBuilder()
        Scanner(File(file)).use { scanner ->
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                stringBuilder.append(line).append(RECORD_SEPARATER)
            }
        }
        return stringBuilder.toString()
    }
}

