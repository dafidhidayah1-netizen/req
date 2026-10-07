package com.example

import com.example.data.model.CvekCalculation
import com.example.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testRupiahFormat() {
        val formatted = FormatUtils.formatRupiah(1500000.0)
        assertTrue(formatted.contains("1.500.000") || formatted.contains("1,500,000") || formatted.contains("1500000"))
        assertTrue(formatted.startsWith("Rp"))
    }

    @Test
    fun testTransactionIdGeneration() {
        val kmId = FormatUtils.generateTransactionId("KM")
        val kkId = FormatUtils.generateTransactionId("KK")
        val trId = FormatUtils.generateTransactionId("TR")

        assertTrue(kmId.startsWith("KM-"))
        assertTrue(kkId.startsWith("KK-"))
        assertTrue(trId.startsWith("TR-"))
        assertEquals(kmId.length, kkId.length)
    }

    @Test
    fun testCvekCalculation() {
        val initial = 10000000.0
        val income = 15000000.0
        val fixed = 4000000.0
        val variable = 2500000.0

        val totalExpense = fixed + variable
        val margin = income - totalExpense
        val pctExpense = (totalExpense / income) * 100
        val finalBalance = initial + margin

        val cvek = CvekCalculation(
            initialBalance = initial,
            totalIncome = income,
            fixedCosts = fixed,
            variableCosts = variable,
            totalExpense = totalExpense,
            netMargin = margin,
            expensePercentage = pctExpense,
            finalBalance = finalBalance
        )

        assertEquals(6500000.0, cvek.totalExpense, 0.001)
        assertEquals(8500000.0, cvek.netMargin, 0.001)
        assertEquals(18500000.0, cvek.finalBalance, 0.001)
        assertTrue(cvek.expensePercentage < 50.0)
    }

    @Test
    fun testDateFormatting() {
        val date = FormatUtils.getCurrentDate()
        val time = FormatUtils.getCurrentTime()
        assertNotNull(date)
        assertNotNull(time)
        assertTrue(date.contains("/"))
        assertTrue(time.contains(":"))
    }
}
