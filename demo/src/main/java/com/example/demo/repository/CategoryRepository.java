package com.example.demo.repository;


import com.example.demo.model.Category;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.ConnectionCallback;

@Repository
public class CategoryRepository {

    private final JdbcTemplate jdbc;

    public CategoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final org.springframework.jdbc.core.RowMapper<Category> mapper =
        (rs, rowNum) -> new Category(
            rs.getInt("CategoryID"),
            rs.getString("CategoryName"),
            rs.getString("Description"),
            rs.getString("Status")
        );

    public List<Category> findAll() {
        return jdbc.query(
            "SELECT CategoryID, CategoryName, Description, Status " +
            "FROM Categories ORDER BY CategoryName",
            mapper
        );
    }

    public Optional<Category> findById(int id) {
        List<Category> result = jdbc.query(
            "SELECT CategoryID, CategoryName, Description, Status " +
            "FROM Categories WHERE CategoryID = ?",
            mapper,
            id
        );

        return result.stream().findFirst();
    }

    public boolean existsByName(String name, Integer excludeId) {
        String sql =
            "SELECT COUNT(*) FROM Categories " +
            "WHERE CategoryName = ?";

        if (excludeId != null) {
            sql += " AND CategoryID <> ?";
            Integer count = jdbc.queryForObject(
                sql, Integer.class, name, excludeId
            );
            return count != null && count > 0;
        }

        Integer count = jdbc.queryForObject(
            sql, Integer.class, name
        );
        return count != null && count > 0;
    }

    public boolean hasBooks(int categoryId) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM Books WHERE CategoryID = ?",
            Integer.class,
            categoryId
        );

        return count != null && count > 0;
    }

    public int insertAndGetId(
        String name,
        String description,
        String status
    ) {
        
        return jdbc.execute((ConnectionCallback<Integer>)connection -> {
            String sql =
                "INSERT INTO Categories " +
                "(CategoryName, Description, Status) VALUES (?, ?, ?)";

            try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, description);
                ps.setString(3, status);
                ps.executeUpdate();
            }

            try (Statement statement = connection.createStatement();
                 ResultSet rs =
                     statement.executeQuery("SELECT @@IDENTITY")) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            throw new java.sql.SQLException(
                "Không lấy được CategoryID vừa tạo"
            );
        });
    }

    public int update(
        int id,
        String name,
        String description,
        String status
    ) {
        return jdbc.update(
            "UPDATE Categories SET CategoryName = ?, " +
            "Description = ?, Status = ? WHERE CategoryID = ?",
            name, description, status, id
        );
    }

    public int delete(int id) {
        return jdbc.update(
            "DELETE FROM Categories WHERE CategoryID = ?",
            id
        );
    }
}
