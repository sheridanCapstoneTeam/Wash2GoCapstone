package project.sheridancollege.wash2goproject

import org.junit.Test

import org.junit.Assert.*
import project.sheridancollege.wash2goproject.ui.maps.MapUtil

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
    fun calculateElapsedTime(){
       assertEquals("0",MapUtil.calculateElapsedTime(3L, 7L))
    }
}