package com.rachmad.app.mychallengetest

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun findStringArray(){
        val data = listOf("test", "data", "array")
        val find = "test" in data
        System.out.println("FIND DATA : " + find)
    }
}