package com.tiketefactura.data

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class SaleTotals(val subtotal: Double, val discount: Double, val tax: Double, val total: Double)

object BusinessRules {
    fun key(value: String): String = java.text.Normalizer.normalize(value.trim(), java.text.Normalizer.Form.NFKC)
        .replace(Regex("\\s+"), " ").lowercase(Locale.ROOT)
    fun phoneKey(value: String?): String = value.orEmpty().filter(Char::isDigit)
    fun validateClient(name: String, phone: String?, email: String?) {
        require(name.isNotBlank()) { "Escribe el nombre del cliente" }
        require(phone.isNullOrBlank() || phoneKey(phone).length in 7..15) { "El teléfono debe tener entre 7 y 15 dígitos" }
        require(email.isNullOrBlank() || Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())) { "El correo no es válido" }
    }
    fun lineTotal(price: Double, quantity: Double): Double {
        require(price.isFinite() && quantity.isFinite()) { "Precio o cantidad inválidos" }
        return BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP).toDouble()
    }
    fun amount(value: Double): Double {
        require(value.isFinite()) { "El importe no es válido" }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    fun totals(lines: List<Pair<Double, Double>>, discount: Double, taxRate: Double): SaleTotals {
        require(lines.isNotEmpty()) { "Agrega productos a la venta" }
        require(lines.all { (price, qty) -> price.isFinite() && price >= 0 && qty.isFinite() && qty > 0 }) { "Precio o cantidad inválidos" }
        require(taxRate.isFinite() && taxRate in 0.0..100.0) { "El impuesto debe estar entre 0 y 100" }
        val subtotal = amount(lines.sumOf { (price, qty) -> lineTotal(price, qty) })
        require(discount.isFinite() && discount >= 0 && discount <= subtotal) { "El descuento debe estar entre cero y el subtotal" }
        val roundedDiscount = amount(discount)
        val base = BigDecimal.valueOf(subtotal).subtract(BigDecimal.valueOf(roundedDiscount))
        val tax = base.multiply(BigDecimal.valueOf(taxRate)).movePointLeft(2).setScale(2, RoundingMode.HALF_UP).toDouble()
        return SaleTotals(subtotal, roundedDiscount, tax, amount(subtotal - roundedDiscount + tax))
    }

    fun payment(balance: Double, value: Double): Double {
        require(value.isFinite() && amount(value) > 0 && amount(value) <= amount(balance)) { "El abono debe ser mayor a cero y no superar el saldo" }
        return amount(balance - amount(value))
    }

    // Legacy records did not store a year. Keep that limitation visible instead of inventing one.
    fun datePart(value: String): LocalDate? = runCatching { LocalDate.parse(value.take(10)) }.getOrNull()
    fun csvCell(value: Any?): String = "\"${value.toString().replace("\"", "\"\"")}\""
    fun now(): String = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT))
}
