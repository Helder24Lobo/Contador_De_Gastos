package com.personalfinance.contador.repository;

import com.personalfinance.contador.model.Gasto;
import com.personalfinance.contador.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GastoDAO {

    public void insert(Gasto gasto) throws SQLException {
        String sql = "INSERT INTO gastos (fecha, descripcion, categoria, valor, observacion) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, gasto.getFecha().toString());
            pstmt.setString(2, gasto.getDescripcion());
            pstmt.setString(3, gasto.getCategoria());
            pstmt.setDouble(4, gasto.getValor());
            pstmt.setString(5, gasto.getObservacion());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    gasto.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void update(Gasto gasto) throws SQLException {
        String sql = "UPDATE gastos SET fecha = ?, descripcion = ?, categoria = ?, valor = ?, observacion = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, gasto.getFecha().toString());
            pstmt.setString(2, gasto.getDescripcion());
            pstmt.setString(3, gasto.getCategoria());
            pstmt.setDouble(4, gasto.getValor());
            pstmt.setString(5, gasto.getObservacion());
            pstmt.setInt(6, gasto.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM gastos WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public Gasto findById(int id) throws SQLException {
        String sql = "SELECT * FROM gastos WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGasto(rs);
                }
            }
        }
        return null;
    }

    public List<Gasto> findAll() throws SQLException {
        String sql = "SELECT * FROM gastos ORDER BY fecha DESC, id DESC";
        List<Gasto> list = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToGasto(rs));
            }
        }
        return list;
    }

    public List<Gasto> findByFilters(LocalDate start, LocalDate end, String categoria, String search) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM gastos WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (start != null) {
            sql.append(" AND fecha >= ?");
            params.add(start.toString());
        }
        if (end != null) {
            sql.append(" AND fecha <= ?");
            params.add(end.toString());
        }
        if (categoria != null && !categoria.equalsIgnoreCase("Todas") && !categoria.trim().isEmpty()) {
            sql.append(" AND categoria = ?");
            params.add(categoria);
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (descripcion LIKE ? OR observacion LIKE ?)");
            String likeParam = "%" + search.trim() + "%";
            params.add(likeParam);
            params.add(likeParam);
        }

        sql.append(" ORDER BY fecha DESC, id DESC");

        List<Gasto> list = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGasto(rs));
                }
            }
        }
        return list;
    }

    public double getTotalGastado(LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT SUM(valor) FROM gastos WHERE fecha >= ? AND fecha <= ?";
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

    public Map<String, Double> getGastosGroupedByCategoria(LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT categoria, SUM(valor) FROM gastos WHERE fecha >= ? AND fecha <= ? GROUP BY categoria";
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
        String sql = "DELETE FROM gastos";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private Gasto mapResultSetToGasto(ResultSet rs) throws SQLException {
        return new Gasto(
                rs.getInt("id"),
                LocalDate.parse(rs.getString("fecha")),
                rs.getString("descripcion"),
                rs.getString("categoria"),
                rs.getDouble("valor"),
                rs.getString("observacion")
        );
    }
}
