package com.zaidsiddique.fieldops

import com.zaidsiddique.fieldops.data.CheckIn
import com.zaidsiddique.fieldops.data.CheckInDao
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckInRepositoryTest {

    @Test
    fun `getAll returns dao flow`() = runBlocking {

        val dao = mockk<CheckInDao>()

        val sample = listOf(
            CheckIn(
                qrCode = "TEST-1",
                timestamp = 0L,
                latitude = 0.0,
                longitude = 0.0,
                address = null,
                photoPath = ""
            )
        )

        every {
            dao.getAll()
        } returns flowOf(sample)

        val result = dao.getAll().first()

        assertEquals(1, result.size)

        assertEquals(
            "TEST-1",
            result.first().qrCode
        )
    }
}