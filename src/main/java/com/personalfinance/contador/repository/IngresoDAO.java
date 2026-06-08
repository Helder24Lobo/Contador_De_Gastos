package com.personalfinance.contador.repository;

import com.personalfinance.contador.model.Income;
import com.personalfinance.contador.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngresoDAO {

    public void insert(Income income) throws SQLException {
        String sql = "INSERT INTO ingresos (fecha, descripcion, valor, tipo) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, income.getFecha().toString());
            pstmt.setString(2, income.getDescripcion());
            pstmt.setDouble(3, income.getValor());
            pstmt.setString(4, income.getTipo());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    income.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void update(Income income) throws SQLException {
        String sql = "UPDATE ingresos SET fecha = ?, descripcion = ?, valor = ?, tipo = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, income.getFecha().toString());
            pstmt.setString(2, income.getDescripcion());
            pstmt.setDouble(3, income.getValor());
            pstmt.setString(4, income.getTipo());
            pstmt.setInt(5, income.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM ingresos WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public Income findById(int id) throws SQLException {
        String sql = "SELECT * FROM ingresos WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToIngreso(rs);
                }
            }
        }
        return null;
    }

    public List<Income> findAll() throws SQLException {
        String sql = "SELECT * FROM ingresos ORDER BY fecha DESC, id DESC";
        List<Income> list = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToIngreso(rs));
            }
        }
        return list;
    }

    public List<Income> findByFilters(LocalDate start, LocalDate end, String type, String search) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM ingresos WHERE 1=1");
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

        List<Income> list = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToIngreso(rs));
                }
            }
        }
        return list;
    }

    public double getTotalIngresado(LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT SUM(valor) FROM ingresos WHERE fecha >= ? AND fecha <= ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, start.toString());
            pstmt.setString(2, end.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public Map<String, Double> getIngresosGroupedByTipo(LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT tipo, SUM(valor) FROM ingresos WHERE fecha >= ? AND fecha <= ? GROUP BY tipo";
        Map<String, Double> map = new HashMap<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, start.toString());
            pstmt.setString(2, end.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString(1), rs.getDouble(2));
                }
            }
        }
        return map;
    }

    public void clearAll() throws SQLException {
        String sql = "DELETE FROM ingresos";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private Income mapResultSetToIngreso(ResultSet rs) throws SQLException {
        return new Income(
                rs.getInt("id"),
                LocalDate.parse(rs.getString("fecha")),
                rs.getString("descripcion"),
                rs.getDouble("valor"),
                rs.getString("tipo")
        );
    }
}
