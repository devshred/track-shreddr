package org.devshred.tracks.utils

import java.io.IOException
import java.util.Properties

object Config {
    private val properties = Properties()

    init {
        val inputStream = Thread.currentThread().contextClassLoader.getResourceAsStream("config.properties")
        try {
            properties.load(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getProp(key: String) = properties.getProperty(key)!!
}
