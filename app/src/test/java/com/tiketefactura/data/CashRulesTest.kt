package com.tiketefactura.data

import com.tiketefactura.data.local.*
import org.junit.Assert.assertEquals
import org.junit.Test

class CashRulesTest {
    @Test fun countsCashPaymentsExpensesAndRefundsWithoutChangingOldBoxes() {
        val box = Caja(id = 2, apertura = "2026-10-17 10:00:00", fondo = 100.0)
        fun doc(id: Int, boxId: Int, method: String, total: Double, state: String = "PAGADA") = DocumentoConItems(
            Documento(id = id, tipo = "TICKET", numero = "T-$id", fecha = "2026-10-17 10:10:00", cajaId = boxId, metodoPago = method, total = total, estado = state), emptyList())
        val docs = listOf(doc(1, 1, "EFECTIVO", 90.0), doc(2, 2, "EFECTIVO", 40.0, "ANULADA"), doc(3, 2, "TARJETA", 200.0))
        val payments = listOf(Abono(clienteId = 1, documentoId = 1, monto = 20.0, fecha = "2026-10-17", cajaId = 2))
        val expenses = listOf(Gasto(descripcion = "Gasto", monto = 10.0, fecha = "2026-10-17", categoria = "Otros", cajaId = 2))
        val refunds = listOf(Reembolso(documentoId = 2, monto = 40.0, metodo = "EFECTIVO", fecha = "2026-10-17", cajaId = 2, motivo = "Devolución"))
        assertEquals(110.0, BusinessRepository.expectedCash(box, docs, payments, expenses, refunds), 0.0001)
    }
}
