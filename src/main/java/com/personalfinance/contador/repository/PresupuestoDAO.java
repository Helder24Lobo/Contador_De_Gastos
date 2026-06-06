package com.personalfinance.contador.repository;

import com.personalfinance.contador.model.Presupuesto;
import com.personalfinance.contador.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PresupuestoDAO {

    public void save(Presupuesto presupuesto) throws SQLException {
        // ON CONFLICT(categoria) permite hacer upsert automáticamente en SQLite moderno
        String sql = "INSERT INTO presupuestos (categoria, valor_presupuestado, fecha_creacion) VALUES (?, ?, ?) " +
                     "ON CONFLICT(categoria) DO UPDATE SET valor_presupuestado = excluded.valor_presupuestado, fecha_creacion = excluded.fecha_creacion";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, presupuesto.getCategoria());
            pstmt.setDouble(2, presupuesto.getValorPresupuestado());
            pstmt.setString(3, presupuesto.getFechaCreacion().toString());
            pstmt.executeUpdate();

            // Si es una inserción nueva, recuperamos el ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    presupuesto.setId(generatedKeys.getInt(1));
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

    public Presupuesto findByCategoria(String categoria) throws SQLException {
        String sql = "SELECT * FROM presupuestos WHERE categoria = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoria);
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
