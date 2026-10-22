package com.example.demo.repository;



import com.example.demo.dto.UserRequest;
import com.example.demo.model.UserModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<UserModel> mapper = (rs, row) ->
        new UserModel(
            rs.getString("username"),
            rs.getString("password"),
            rs.getBoolean("status")
        );

    public List<UserModel> findAll() {
        return jdbc.query(
            "SELECT username, [password], status FROM users",
            mapper
        );
    }

    public Optional<UserModel> findByUsername(String username) {
        return jdbc.query(
            "SELECT username, [password], status FROM users WHERE username=?",
            mapper, username
        ).stream().findFirst();
    }

    public boolean exists(String username) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE username=?",
            Integer.class, username
        );
        return count != null && count > 0;
    }

    public int insert(UserRequest req, String encodedPassword) {
        return jdbc.update(
            "INSERT INTO users (username, [password], status) VALUES (?, ?, ?)",
            req.username(), encodedPassword,
            req.status() == null || req.status()
        );
    }

    public int update(String username, UserRequest req,
                      String encodedPassword) {
        if (encodedPassword != null) {
            return jdbc.update(
                "UPDATE users SET [password]=?, status=? WHERE username=?",
                encodedPassword,
                req.status() == null || req.status(),
                username
            );
        }

        return jdbc.update(
            "UPDATE users SET status=? WHERE username=?",
            req.status() == null || req.status(),
            username
        );
    }

    public int delete(String username) {
        return jdbc.update(
            "DELETE FROM users WHERE username=?", username
        );
    }
}
