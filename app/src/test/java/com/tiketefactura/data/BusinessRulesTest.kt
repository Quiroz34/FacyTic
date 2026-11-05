package com.tiketefactura.data

import org.junit.Assert.*
import org.junit.Test

class BusinessRulesTest {
    @Test fun duplicateKeysIgnoreWhitespaceAndCaseButPreserveVariantNames() {
        assertEquals(BusinessRules.key("  CAFE   GRANDE "), BusinessRules.key("cafe grande"))
        assertNotEquals(BusinessRules.key("Café grande"), BusinessRules.key("Café chico"))
        assertEquals("5512345678", BusinessRules.phoneKey("55 1234-5678"))
    }
    @Test fun validatesContactFieldsWithoutRequiringOptionalOnes() {
        BusinessRules.validateClient("Ana", null, null)
        BusinessRules.validateClient("Ana", "55 1234-5678", "ana@example.com")
        assertThrows(IllegalArgumentException::class.java) { BusinessRules.validateClient(" ", null, null) }
        assertThrows(IllegalArgumentException::class.java) { BusinessRules.validateClient("Ana", "123", null) }
        assertThrows(IllegalArgumentException::class.java) { BusinessRules.validateClient("Ana", null, "correo inválido") }
    }
    @Test fun roundsLinesDiscountAndTaxConsistently() {
        val totals = BusinessRules.totals(listOf(19.99 to 1.5, 0.1 to 3.0), 2.0, 16.0)
        assertEquals(30.29, totals.subtotal, 0.0001)
        assertEquals(4.53, totals.tax, 0.0001)
        assertEquals(32.82, totals.total, 0.0001)
    }
    @Test fun rejectsDiscountBeyondSubtotal() {
        assertThrows(IllegalArgumentException::class.java) { BusinessRules.totals(listOf(10.0 to 1.0), 11.0, 0.0) }
    }
    @Test fun rejectsNonFiniteAndNegativeAmounts() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, -1.0).forEach { value ->
            assertThrows(IllegalArgumentException::class.java) { BusinessRules.totals(listOf(value to 1.0), 0.0, 15.0) }
            assertThrows(IllegalArgumentException::class.java) { BusinessRules.payment(100.0, value) }
        }
    }
    @Test fun paymentCannotIncreaseDebtOrOverpay() {
        assertEquals(69.9, BusinessRules.payment(100.0, 30.1), 0.0001)
        assertThrows(IllegalArgumentException::class.java) { BusinessRules.payment(100.0, 101.0) }
        assertThrows(IllegalArgumentException::class.java) { BusinessRules.payment(100.0, 0.001) }
    }
    @Test fun keepsLegacyDatesUnknownAndEscapesCsv() {
        assertNull(BusinessRules.datePart("27 ago, 13:42"))
        assertEquals("2026-10-17", BusinessRules.datePart("2026-10-17 12:00:00").toString())
        assertEquals("\"Nombre, \"\"especial\"\"\"", BusinessRules.csvCell("Nombre, \"especial\""))
    }
}
