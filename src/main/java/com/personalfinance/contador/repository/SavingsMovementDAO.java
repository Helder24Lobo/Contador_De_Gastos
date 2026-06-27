package com.personalfinance.contador.repository;

import com.personalfinance.contador.model.SavingsMovement;
import com.personalfinance.contador.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SavingsMovementDAO {

    public void insert(SavingsMovement movement) throws SQLException {
        String sql = "INSERT INTO savings_movements (saving_id, amount, date, observation) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, movement.getSavingId());
            pstmt.setDouble(2, movement.getAmount());
            pstmt.setString(3, movement.getDate().toString());
            pstmt.setString(4, movement.getObservation());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    movement.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<SavingsMovement> findBySavingId(int savingId) throws SQLException {
        String sql = "SELECT * FROM savings_movements WHERE saving_id = ? ORDER BY date DESC, id DESC";
        List<SavingsMovement> list = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, savingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new SavingsMovement(
                            rs.getInt("id"),
                            rs.getInt("saving_id"),
                            rs.getDouble("amount"),
                            LocalDate.parse(rs.getString("date")),
                            rs.getString("observation")
                    ));
                }
            }
        }
        return list;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM savings_movements WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}
