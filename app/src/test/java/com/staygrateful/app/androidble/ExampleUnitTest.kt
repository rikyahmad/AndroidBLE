package com.staygrateful.app.androidble

import com.staygrateful.app.androidble.util.StringUtils
import org.junit.Test

import org.junit.Assert.*

class ExampleUnitTest {

    @Test
    fun joinTest() {
        val list = listOf(1, 2, 3, 4, 5)
        val result = list.joinToString(
            separator = " -- ",
            prefix = "<pre",
            postfix = "pos>"
        ) { data ->
            "Number : $data"
        }
        val result2 = StringUtils.joinToString(
            list,
            " -- ",
            "<pre",
            "pos>",
        ) { data ->
            "Number : $data"
        }
        println("Data : $result")
        println("Data : $result2")
        assertNotNull(result2)
    }
}