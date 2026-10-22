package com.example.demo.repository;



import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record Account(
        long userId,
        String username,
        String email,
        String passwordHash,
        String fullName,
        String role,
        String status
    ) {}

    private final RowMapper<Account> mapper = this::mapAccount;

    private Account mapAccount(ResultSet rs, int rowNum)
            throws SQLException {

        return new Account(
            rs.getLong("UserID"),
            rs.getString("Username"),
            rs.getString("Email"),
            rs.getString("PasswordHash"),
            rs.getString("FullName"),
            rs.getString("Role"),
            rs.getString("Status")
        );
    }

    public Optional<Account> findByUsernameOrEmail(String value) {
        String sql = """
            SELECT UserID, Username, Email, PasswordHash,
                   FullName, Role, Status
            FROM Users
            WHERE Username = ? OR Email = ?
            """;

        try {
            return Optional.ofNullable(
                jdbcTemplate.queryForObject(
                    sql, mapper, value, value
                )
            );
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Users WHERE Username = ?",
            Integer.class,
            username
        );

        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Users WHERE Email = ?",
            Integer.class,
            email
        );

        return count != null && count > 0;
    }

    public long create(
            String username,
            String email,
            String passwordHash,
            String fullName) {

        String sql = """
            INSERT INTO Users
                (Username, Email, PasswordHash, FullName,
                 Role, Status, CreatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        Timestamp now = new Timestamp(System.currentTimeMillis());

        jdbcTemplate.update(
            sql,
            username,
            email,
            passwordHash,
            fullName,
            "CUSTOMER",
            "ACTIVE",
            now
        );

        // Access tạo UserID bằng AutoNumber.
        // Đọc lại bản ghi vừa tạo để lấy ID thực tế.
        return findByUsernameOrEmail(username)
            .orElseThrow(() ->
                new IllegalStateException(
                    "Không tìm thấy tài khoản vừa tạo"
                )
            )
            .userId();
    }
}