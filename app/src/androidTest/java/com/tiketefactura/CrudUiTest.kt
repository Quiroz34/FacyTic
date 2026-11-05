package com.tiketefactura

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CrudUiTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private fun waitFor(text: String) = ui.waitUntil(30_000) { ui.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty() }
    private fun tap(text: String) { waitFor(text); ui.onNodeWithText(text).performClick() }
    private fun field(label: String, value: String) {
        val node = ui.onNode(hasSetTextAction() and hasText(label))
        runCatching { node.performScrollTo() }
        node.performTextReplacement(value)
    }
    private fun nav(title: String) {
        ui.waitUntil(30_000) { ui.onAllNodesWithContentDescription("Abrir menú").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithContentDescription("Abrir menú").performClick()
        val destinations = ui.onAllNodes(hasText(title) and hasClickAction())
        ui.waitUntil(30_000) { destinations.fetchSemanticsNodes().isNotEmpty() }
        destinations[destinations.fetchSemanticsNodes().lastIndex].performClick()
        ui.waitForIdle()
    }
    private fun saved() { ui.waitUntil(30_000) { ui.onAllNodesWithText("Guardar", substring = false).fetchSemanticsNodes().isEmpty() }; ui.waitForIdle() }

    @Test fun createEditDuplicateValidateSellPayAndDelete() {
        waitFor("Inicio")
        nav("Productos"); tap("Agregar producto")
        field("Nombre", "UI Producto"); field("Precio de venta", "10"); field("Costo de compra", "4"); field("Stock inicial", "5"); field("Código de barras", "UI123")
        tap("Guardar"); saved(); waitFor("UI Producto")
        tap("Editar"); field("Nombre", "UI Producto editado"); tap("Guardar"); saved(); waitFor("UI Producto editado")
        tap("Agregar producto"); field("Nombre", "UI Producto editado"); tap("Guardar")
        waitFor("Revisa la operación"); tap("Entendido"); tap("Cancelar")
        tap("Categorías"); field("Nombre", "UI Categoría"); tap("Agregar"); waitFor("UI Categoría")
        ui.onNodeWithContentDescription("Editar").performClick(); field("Nombre", "UI Categoría editada"); tap("Guardar cambios"); waitFor("UI Categoría editada")
        ui.onNodeWithContentDescription("Eliminar").performClick(); tap("Confirmar"); tap("Cerrar")

        nav("Clientes"); tap("Nuevo cliente"); field("Nombre", "UI Cliente"); field("Teléfono", "5512345678"); field("Correo", "ui@example.com"); tap("Guardar"); saved()
        tap("Editar"); field("Nombre", "UI Cliente editado"); tap("Guardar"); saved(); waitFor("UI Cliente editado")

        nav("Inventario"); tap("Entrada / ajuste"); field("Cantidad: positiva entra, negativa sale", "-20"); field("Motivo / referencia de compra", "UI ajuste"); tap("Guardar")
        waitFor("Revisa la operación"); tap("Entendido"); field("Cantidad: positiva entra, negativa sale", "2"); tap("Guardar"); saved()
        tap("Movimientos"); waitFor("ENTRADA: UI ajuste")

        nav("Caja"); tap("Abrir caja"); field("Fondo inicial de efectivo", "100"); tap("Guardar"); saved()
        nav("Nueva venta"); ui.onNodeWithContentDescription("Agregar UI Producto editado").performScrollTo().performClick()
        ui.onNodeWithText("Seleccionar cliente registrado").performScrollTo().performClick(); tap("UI Cliente editado · saldo $0.00")
        ui.onNodeWithText("CRÉDITO").performScrollTo().performClick()
        ui.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Confirmar venta"))
        ui.onNodeWithText("Confirmar venta").performClick(); tap("Guardar venta"); waitFor("T-00000001")
        ui.onNodeWithText("Imprimir ticket Bluetooth").assertExists(); ui.onNodeWithText("Compartir PDF").assertExists(); tap("Cerrar")

        nav("Clientes"); tap("Estado de cuenta"); field("Monto del abono", "-1"); tap("Registrar abono"); waitFor("Revisa la operación"); tap("Entendido")
        field("Monto del abono", "5"); tap("Registrar abono"); waitFor("Abono registrado"); tap("Cerrar"); tap("Cerrar")
        tap("Eliminar"); tap("Confirmar"); waitFor("Revisa la operación"); tap("Entendido"); tap("Cancelar")

        nav("Historial"); tap("Ver comprobante"); ui.onNodeWithText("Imprimir ticket Bluetooth").performScrollTo().performClick(); waitFor("Impresora térmica"); tap("Cerrar"); tap("Cerrar")
        tap("Cancelar / devolver"); field("Motivo y confirmación del reembolso", "UI devolución completa"); tap("Guardar"); saved(); waitFor("CRÉDITO · ANULADA")
        nav("Gastos"); tap("Registrar gasto"); field("Descripción", "UI gasto"); field("Monto", "2"); tap("Guardar"); saved()
        tap("Editar"); field("Descripción", "UI gasto editado"); field("Monto", "3"); tap("Guardar"); saved(); waitFor("UI gasto editado")
        tap("Eliminar"); tap("Confirmar"); waitFor("Aún no hay gastos registrados")
        nav("Caja"); tap("Cerrar y contar caja"); field("Efectivo contado", "100"); tap("Guardar"); saved(); waitFor("Cierre #1")

        nav("Productos"); tap("Agregar producto"); field("Nombre", "UI Temporal"); tap("Guardar"); saved()
        field("Buscar por nombre o código", "UI Temporal"); tap("Eliminar"); tap("Confirmar"); waitFor("No hay productos.")
        nav("Clientes"); tap("Nuevo cliente"); field("Nombre", "UI Temporal"); tap("Guardar"); saved(); field("Buscar cliente o teléfono", "UI Temporal"); tap("Eliminar"); tap("Confirmar")
        nav("Ajustes"); ui.onNodeWithText("Configurar impresora Bluetooth").performScrollTo().performClick(); waitFor("Impresora térmica"); tap("Cerrar")
        ui.onNodeWithText("Guardar respaldo completo").performScrollTo().assertExists()
        nav("Inicio"); ui.onNodeWithText("Resumen").assertExists()
    }
}
