package com.personalfinance.contador.repository;

import com.personalfinance.contador.model.Savings;
import com.personalfinance.contador.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para la entidad {@link Savings} (Metas de Ahorro).
 */
public class SavingsDAO {

    /**
     * Inserta una nueva meta de ahorro.
     */
    public void insert(Savings savings) throws SQLException {
        String sql = "INSERT INTO savings (name, description, target_value, dateCurrent, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, savings.getName());
            pstmt.setString(2, savings.getDescription());
            pstmt.setDouble(3, savings.getTargetValue());
            pstmt.setString(4, savings.getDateCurrent().toString());
            pstmt.setString(5, savings.getStatus());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    savings.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Actualiza una meta de ahorro existente.
     */
    public void update(Savings savings) throws SQLException {
        String sql = "UPDATE savings SET name = ?, description = ?, target_value = ?, dateCurrent = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, savings.getName());
            pstmt.setString(2, savings.getDescription());
            pstmt.setDouble(3, savings.getTargetValue());
            pstmt.setString(4, savings.getDateCurrent().toString());
            pstmt.setString(5, savings.getStatus());
            pstmt.setInt(6, savings.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Elimina una meta de ahorro y todos sus movimientos (por cascada).
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM savings WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Recupera todas las metas de ahorro.
     */
    public List<Savings> findAll() throws SQLException {
        return findByFilters(null, null, "Todos", null);
    }

    /**
     * Convierte la fila actual de un ResultSet en una instancia de Savings.
     */
    private Savings mapResultSetToSavings(ResultSet rs) throws SQLException {
        Savings savings = new Savings(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("target_value"),
                LocalDate.parse(rs.getString("dateCurrent")),
                rs.getString("status")
        );
        savings.setSavedAmount(rs.getDouble("saved_amount"));
        return savings;
    }

    /**
     * Busca registros aplicando filtros opcionales.
     */
    public List<Savings> findByFilters(LocalDate start, LocalDate end, String status, String search) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT s.id, s.name, s.description, s.target_value, s.dateCurrent, s.status, COALESCE(SUM(m.amount), 0) AS saved_amount " +
                "FROM savings s " +
                "LEFT JOIN savings_movements m ON s.id = m.saving_id " +
                "WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (start != null) {
            sql.append(" AND s.dateCurrent >= ?");
            params.add(start.toString());
        }
        if (end != null) {
            sql.append(" AND s.dateCurrent <= ?");
            params.add(end.toString());
        }
        if (status != null && !status.equalsIgnoreCase("Todos") && !status.trim().isEmpty()) {
            sql.append(" AND s.status = ?");
            params.add(status);
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (s.name LIKE ? OR s.description LIKE ?)");
            String likeParam = "%" + search.trim() + "%";
            params.add(likeParam);
            params.add(likeParam);
        }

        sql.append(" GROUP BY s.id ORDER BY s.dateCurrent DESC, s.id DESC");

        List<Savings> list = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSavings(rs));
                }
            }
        }
        return list;
    }

    /**
     * Obtiene el dinero ahorrado total de una meta específica para recálculos rápidos.
     */
    public double getSavedAmount(int savingId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM savings_movements WHERE saving_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, savingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    /**
     * Actualiza el estado de una meta de ahorro.
     */
    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE savings SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }
}
