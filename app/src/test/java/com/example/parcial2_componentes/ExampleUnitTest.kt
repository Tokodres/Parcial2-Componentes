package com.example.parcial2_componentes

import org.junit.Test
import org.junit.Assert.*
import com.example.parcial2_componentes.data.model.Payment
import com.example.parcial2_componentes.data.model.Member

class PaymentCalculationsTest {

    @Test
    fun testCalculateTotalCollected() {
        // Given: Lista de pagos de prueba
        val payments = listOf(
            Payment(amount = 100.0, memberId = "1", planId = "plan1"),
            Payment(amount = 200.0, memberId = "2", planId = "plan1"),
            Payment(amount = 150.0, memberId = "1", planId = "plan1"),
            Payment(amount = 75.0, memberId = "3", planId = "plan1")
        )

        // When: Calculamos el total recaudado
        val totalCollected = calculateTotalCollected(payments)

        // Then: Verificamos que la suma sea correcta
        assertEquals(525.0, totalCollected, 0.01)
    }

    @Test
    fun testCalculateTotalCollectedWithEmptyList() {
        // Given: Lista vacía de pagos
        val payments = emptyList<Payment>()

        // When: Calculamos el total recaudado
        val totalCollected = calculateTotalCollected(payments)

        // Then: Verificamos que sea 0
        assertEquals(0.0, totalCollected, 0.01)
    }

    @Test
    fun testCalculatePaymentsByMember() {
        // Given: Miembros y pagos de prueba
        val members = listOf(
            Member(_id = "1", name = "Juan", planId = "plan1"),
            Member(_id = "2", name = "Maria", planId = "plan1"),
            Member(_id = "3", name = "Pedro", planId = "plan1")
        )

        val payments = listOf(
            Payment(amount = 100.0, memberId = "1", planId = "plan1"),
            Payment(amount = 200.0, memberId = "2", planId = "plan1"),
            Payment(amount = 150.0, memberId = "1", planId = "plan1"), // Segundo pago de Juan
            Payment(amount = 75.0, memberId = "3", planId = "plan1"),
            Payment(amount = 50.0, memberId = "2", planId = "plan1")  // Segundo pago de Maria
        )

        // When: Calculamos pagos por miembro
        val paymentsByMember = calculatePaymentsByMember(members, payments)

        // Then: Verificamos los totales por miembro
        assertEquals(250.0, paymentsByMember[members[0]] ?: 0.0, 0.01) // Juan: 100 + 150
        assertEquals(250.0, paymentsByMember[members[1]] ?: 0.0, 0.01) // Maria: 200 + 50
        assertEquals(75.0, paymentsByMember[members[2]] ?: 0.0, 0.01)  // Pedro: 75

        // Verificamos que todos los miembros estén en el mapa
        assertEquals(3, paymentsByMember.size)
    }

    @Test
    fun testCalculatePaymentsByMemberWithNoPayments() {
        // Given: Miembros pero sin pagos
        val members = listOf(
            Member(_id = "1", name = "Juan", planId = "plan1"),
            Member(_id = "2", name = "Maria", planId = "plan1")
        )

        val payments = emptyList<Payment>()

        // When: Calculamos pagos por miembro
        val paymentsByMember = calculatePaymentsByMember(members, payments)

        // Then: Verificamos que todos los miembros tengan 0
        assertEquals(0.0, paymentsByMember[members[0]] ?: 0.0, 0.01)
        assertEquals(0.0, paymentsByMember[members[1]] ?: 0.0, 0.01)
        assertEquals(2, paymentsByMember.size)
    }

    @Test
    fun testCalculatePaymentsByMemberWithUnmatchedPayments() {
        // Given: Pagos que no coinciden con ningún miembro
        val members = listOf(
            Member(_id = "1", name = "Juan", planId = "plan1")
        )

        val payments = listOf(
            Payment(amount = 100.0, memberId = "999", planId = "plan1"), // ID que no existe
            Payment(amount = 200.0, memberId = "1", planId = "plan1")    // Pago válido
        )

        // When: Calculamos pagos por miembro
        val paymentsByMember = calculatePaymentsByMember(members, payments)

        // Then: Verificamos que solo se cuente el pago válido
        assertEquals(200.0, paymentsByMember[members[0]] ?: 0.0, 0.01)
        assertEquals(1, paymentsByMember.size)
    }

    // Funciones auxiliares para los cálculos (similares a las usadas en el composable)
    private fun calculateTotalCollected(payments: List<Payment>): Double {
        return payments.sumOf { it.amount }
    }

    private fun calculatePaymentsByMember(members: List<Member>, payments: List<Payment>): Map<Member, Double> {
        return members.associate { member ->
            member to payments.filter { it.memberId == member._id }.sumOf { it.amount }
        }
    }
}