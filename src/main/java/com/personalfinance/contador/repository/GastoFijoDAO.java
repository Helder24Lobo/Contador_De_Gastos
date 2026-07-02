package com.personalfinance.contador.repository;

import com.personalfinance.contador.model.FixedExpense;
import com.personalfinance.contador.util.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GastoFijoDAO {

    public void insert(FixedExpense fixedExpense) throws SQLException {
        String sql = "INSERT INTO gastos_fijos (nombre, valor, dia_cobro, estado) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, fixedExpense.getNombre());
            pstmt.setDouble(2, fixedExpense.getValor());
            pstmt.setInt(3, fixedExpense.getDiaCobro());
            pstmt.setString(4, fixedExpense.getEstado());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    fixedExpense.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void update(FixedExpense fixedExpense) throws SQLException {
        String sql = "UPDATE gastos_fijos SET nombre = ?, valor = ?, dia_cobro = ?, estado = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fixedExpense.getNombre());
            pstmt.setDouble(2, fixedExpense.getValor());
            pstmt.setInt(3, fixedExpense.getDiaCobro());
            pstmt.setString(4, fixedExpense.getEstado());
            pstmt.setInt(5, fixedExpense.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM gastos_fijos WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public FixedExpense findById(int id) throws SQLException {
        String sql = "SELECT * FROM gastos_fijos WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGastoFijo(rs);
                }
            }
        }
        return null;
    }

    public List<FixedExpense> findAll() throws SQLException {
        String sql = "SELECT * FROM gastos_fijos ORDER BY nombre ASC";
        List<FixedExpense> list = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToGastoFijo(rs));
            }
        }
        return list;
    }

    public double getTotalGastosFijosActivos() throws SQLException {
        String sql = "SELECT SUM(valor) FROM gastos_fijos";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public void updateEstado(int id, String status) throws SQLException {
        String sql = "UPDATE gastos_fijos SET estado = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    public void clearAll() throws SQLException {
        String sql = "DELETE FROM gastos_fijos";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private FixedExpense mapResultSetToGastoFijo(ResultSet rs) throws SQLException {
        return new FixedExpense(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getDouble("valor"),
                rs.getInt("dia_cobro"),
                rs.getString("estado")
        );
    }
}
