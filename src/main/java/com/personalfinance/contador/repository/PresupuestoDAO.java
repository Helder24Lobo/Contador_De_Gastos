package com.personalfinance.contador.repository;

import com.personalfinance.contador.model.Presupuesto;
import com.personalfinance.contador.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PresupuestoDAO {

    public void save(Presupuesto budget) throws SQLException {
        // ON CONFLICT(categoria) allows automatic upsert in modern SQLite
        String sql = "INSERT INTO presupuestos (categoria, valor_presupuestado, fecha_creacion) VALUES (?, ?, ?) " +
                     "ON CONFLICT(categoria) DO UPDATE SET valor_presupuestado = excluded.valor_presupuestado, fecha_creacion = excluded.fecha_creacion";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, budget.getCategoria());
            pstmt.setDouble(2, budget.getValorPresupuestado());
            pstmt.setString(3, budget.getFechaCreacion().toString());
            pstmt.executeUpdate();

            // If it is a new insertion, retrieve the generated ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    budget.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM presupuestos WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public Presupuesto findByCategoria(String category) throws SQLException {
        String sql = "SELECT * FROM presupuestos WHERE categoria = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPresupuesto(rs);
                }
            }
        }
        return null;
    }

    public List<Presupuesto> findAll() throws SQLException {
        String sql = "SELECT * FROM presupuestos ORDER BY categoria ASC";
        List<Presupuesto> list = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToPresupuesto(rs));
            }
        }
        return list;
    }

    public void clearAll() throws SQLException {
        String sql = "DELETE FROM presupuestos";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private Presupuesto mapResultSetToPresupuesto(ResultSet rs) throws SQLException {
        return new Presupuesto(
                rs.getInt("id"),
                rs.getString("categoria"),
                rs.getDouble("valor_presupuestado"),
                LocalDate.parse(rs.getString("fecha_creacion"))
        );
    }
}
