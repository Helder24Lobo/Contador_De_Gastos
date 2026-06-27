package com.personalfinance.contador.repository;

import com.personalfinance.contador.model.Savings;
import com.personalfinance.contador.model.SavingsMovement;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class SavingsDAOTest {

    @Test
    public void testSavingsModelCalculations() {
        Savings savings = new Savings("Comprar laptop", "Meta de ahorro para laptop", 5000000.0, LocalDate.now(), "Activo");
        
        // Estado inicial
        savings.setSavedAmount(0.0);
        assertEquals(5000000.0, savings.getTargetValue());
        assertEquals(0.0, savings.getSavedAmount());
        assertEquals(5000000.0, savings.getRemainingAmount());
        assertEquals(0.0, savings.getProgressPercentage());
        assertEquals("Activo", savings.getStatus());

        // Ahorrado parcial
        savings.setSavedAmount(2000000.0);
        assertEquals(3000000.0, savings.getRemainingAmount());
        assertEquals(40.0, savings.getProgressPercentage());

        // Totalmente ahorrado
        savings.setSavedAmount(5000000.0);
        assertEquals(0.0, savings.getRemainingAmount());
        assertEquals(100.0, savings.getProgressPercentage());

        // Exceso ahorrado
        savings.setSavedAmount(6000000.0);
        assertEquals(0.0, savings.getRemainingAmount());
        assertEquals(100.0, savings.getProgressPercentage());
    }

    @Test
    public void testSavingsMovementModel() {
        SavingsMovement movement = new SavingsMovement(1, 500000.0, LocalDate.now(), "Abono 1");
        assertEquals(1, movement.getSavingId());
        assertEquals(500000.0, movement.getAmount());
        assertEquals("Abono 1", movement.getObservation());
    }
}
