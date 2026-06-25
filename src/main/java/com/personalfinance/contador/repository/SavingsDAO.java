package com.personalfinance.contador.repository;

import com.personalfinance.contador.model.Savings;
import com.personalfinance.contador.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para la entidad {@link Savings}.
 *
 * <p>
 * Proporciona operaciones básicas de persistencia sobre la tabla "savings":
 * insert, update, búsqueda de todos los registros y búsqueda por filtros.
 * Las conexiones a la base de datos se obtienen mediante {@link DatabaseHelper#getConnection()}
 * y siempre se gestionan con try-with-resources para asegurar el cierre de recursos.
 * </p>
 *
 * <p>
 * Notas importantes:
 * - El campo {@code dateCurrent} se almacena/lee como cadena en formato ISO (yyyy-MM-dd).
 * Por tanto se usa {@link LocalDate#toString()} al insertar/actualizar y {@link LocalDate#parse}
 * al leer del ResultSet.
 * - Las consultas usan {@link PreparedStatement} y parámetros enlazados para evitar inyección SQL.
 * </p>
 *
 * @since 1.0
 */
public class SavingsDAO {
    /**
     * Inserta un nuevo registro {@link Savings} en la tabla "savings".
     *
     * <p>
     * Inserta los campos: dateCurrent, description, priority y amount. Después de la ejecución,
     * si el driver devuelve una clave generada, se asigna a {@code savings.setId(...)}.
     * </p>
     *
     * @param savings objeto {@link Savings} a insertar. Debe contener valores válidos para
     *                dateCurrent, description, priority y amount.
     * @throws SQLException si ocurre un error de acceso a la base de datos o al ejecutar la sentencia.
     */
    public void insert(Savings savings) throws SQLException {
        String sql = "INSERT INTO savings (dateCurrent, description, priority, amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, savings.getDateCurrent().toString());
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

    /**
     * Actualiza un registro existente de {@link Savings} identificado por su id.
     *
     * <p>
     * Actualiza los campos: dateCurrent, description, priority y amount para el registro con el id
     * proporcionado en {@code savings.getId()}.
     * </p>
     *
     * @param savings objeto {@link Savings} con el id del registro a actualizar y los nuevos valores.
     * @throws SQLException si ocurre un error de acceso a la base de datos o al ejecutar la sentencia.
     */
    public void update(Savings savings) throws SQLException {
        String sql = "UPDATE savings SET dateCurrent = ?, description = ?, priority = ?, amount = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, savings.getDateCurrent().toString());
            pstmt.setString(2, savings.getDescription());
            pstmt.setString(3, savings.getPriority());
            pstmt.setDouble(4, savings.getAmount());
            pstmt.setInt(5, savings.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Recupera todos los registros de la tabla "savings".
     *
     * <p>
     * Los resultados se devuelven ordenados por {@code dateCurrent} descendente y luego por {@code id} descendente.
     * </p>
     *
     * @return lista de objetos {@link Savings} (vacía si no hay registros).
     * @throws SQLException si ocurre un error al consultar la base de datos.
     */
    public List<Savings> findAll() throws SQLException {
        String sql = "SELECT * FROM savings ORDER BY dateCurrent DESC, id DESC";
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

    /**
     * Convierte la fila actual de un {@link ResultSet} en una instancia de {@link Savings}.
     *
     * <p>
     * Asume que las columnas existen y que {@code dateCurrent} está almacenado como cadena en formato ISO (yyyy-MM-dd).
     * </p>
     *
     * @param rs ResultSet posicionado en la fila a mapear.
     * @return nueva instancia de {@link Savings} con los valores leídos del ResultSet.
     * @throws SQLException si ocurre un error leyendo las columnas del ResultSet.
     */
    private Savings mapResultSetToSavings(ResultSet rs) throws SQLException {
        return new Savings(
                rs.getInt("id"),
                LocalDate.parse(rs.getString("dateCurrent")),
                rs.getString("description"),
                rs.getString("priority"),
                rs.getDouble("amount")
        );
    }

    /**
     * Busca registros en la tabla "savings" aplicando filtros opcionales.
     *
     * <p>
     * Filtros soportados:
     * <ul>
     *   <li>{@code start} (inclusive): si no es {@code null}, filtra {@code dateCurrent >= start}.</li>
     *   <li>{@code end} (inclusive): si no es {@code null}, filtra {@code dateCurrent <= end}.</li>
     *   <li>{@code type}: si no es {@code null}, diferente de "Todos" y no vacío, filtra por {@code priority = type}.</li>
     *   <li>{@code search}: si no es {@code null} ni vacío, filtra {@code description LIKE '%search%'} (case depending on DB collation).</li>
     * </ul>
     * Los parámetros se enlazan con {@link PreparedStatement} para evitar inyección SQL.
     * </p>
     *
     * <p>
     * El resultado se ordena por {@code dateCurrent} descendente y luego por {@code id} descendente.
     * </p>
     *
     * @param start  fecha de inicio (inclusive) para {@code dateCurrent}. Puede ser {@code null} para no filtrar por inicio.
     * @param end    fecha final (inclusive) para {@code dateCurrent}. Puede ser {@code null} para no filtrar por fin.
     * @param type   tipo/priority para filtrar (por ejemplo "Alta", "Media", "Baja"). Si se pasa "Todos" o cadena vacía se ignora.
     * @param search texto para buscar dentro de {@code description}; se usa LIKE con comodines al inicio y al final.
     * @return lista de {@link Savings} que cumplen los filtros, posiblemente vacía si no hay coincidencias.
     * @throws SQLException si ocurre un error al preparar o ejecutar la consulta.
     */
    public List<Savings> findByFilters(LocalDate start, LocalDate end, String type, String search) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM savings WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (start != null) {
            sql.append(" AND dateCurrent >= ?");
            params.add(start.toString());
        }
        if (end != null) {
            sql.append(" AND dateCurrent <= ?");
            params.add(end.toString());
        }
        if (type != null && !type.equalsIgnoreCase("Todos") && !type.trim().isEmpty()) {
            sql.append(" AND priority = ?");
            params.add(type);
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND description LIKE ?");
            params.add("%" + search.trim() + "%");
        }

        sql.append(" ORDER BY dateCurrent DESC, id DESC");

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
