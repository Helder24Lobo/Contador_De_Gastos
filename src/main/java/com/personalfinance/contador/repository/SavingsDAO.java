package com.personalfinance.contador.repository;

import com.personalfinance.contador.model.Savings;
import com.personalfinance.contador.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SavingsDAO {

    public void insert(Savings savings) throws SQLException {
        String sql = "INSERT INTO savings (fecha, descripcion, valor, tipo) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, savings.getDate().toString());
            pstmt.setString(2, savings.getDescription());
            pstmt.setString(3, savings.getPriority());
            pstmt.setDouble(4, savings.getAmount());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    savings.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void update(Savings savings) throws SQLException {
        String sql = "UPDATE savings SET fecha = ?, descripcion = ?, valor = ?, tipo = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, savings.getDate().toString());
            pstmt.setString(2, savings.getDescription());
            pstmt.setString(3, savings.getPriority());
            pstmt.setDouble(4, savings.getAmount());
            pstmt.setInt(5, savings.getId());
            pstmt.executeUpdate();
        }
    }

    public List<Savings> findAll() throws SQLException {
        String sql = "SELECT * FROM savings ORDER BY fecha DESC, id DESC";
        List<Savings> list = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToSavings(rs));
            }
        }
        return list;
    }

    private Savings mapResultSetToSavings(ResultSet rs) throws SQLException {
        return new Savings(
                rs.getInt("id"),
                LocalDate.parse(rs.getString("fecha")),
                rs.getString("descripcion"),
                rs.getString("valor"),
                rs.getDouble("priority")
        );
    }

    public List<Savings> findByFilters(LocalDate start, LocalDate end, String type, String search) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM savings WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (start != null) {
            sql.append(" AND fecha >= ?");
            params.add(start.toString());
        }
        if (end != null) {
            sql.append(" AND fecha <= ?");
            params.add(end.toString());
        }
        if (type != null && !type.equalsIgnoreCase("Todos") && !type.trim().isEmpty()) {
            sql.append(" AND tipo = ?");
            params.add(type);
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND descripcion LIKE ?");
            params.add("%" + search.trim() + "%");
        }

        sql.append(" ORDER BY fecha DESC, id DESC");

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
}
