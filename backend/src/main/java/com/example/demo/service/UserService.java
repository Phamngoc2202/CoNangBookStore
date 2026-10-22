package com.example.demo.service;



import com.example.demo.dto.UserRequest;
import com.example.demo.model.UserModel;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder =
        new BCryptPasswordEncoder();

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<UserModel> findAll() {
        return repo.findAll();
    }

    public UserModel findByUsername(String username) {
        return repo.findByUsername(username)
            .orElseThrow(() ->
                new IllegalArgumentException("Không tìm thấy user"));
    }

    public UserModel create(UserRequest req) {
        if (repo.exists(req.username())) {
            throw new IllegalArgumentException(
                "Username đã tồn tại");
        }

        //repo.insert(req, encoder.encode(req.password()));
        repo.insert(req,req.password());
        return findByUsername(req.username());
    }

    public UserModel update(String username, UserRequest req) {
        if (!repo.exists(username)) {
            throw new IllegalArgumentException(
                "Không tìm thấy user");
        }

        String encoded = req.password() == null
            || req.password().isBlank()
             ? null : req.password();
            // ? null : encoder.encode(req.password());

        repo.update(username, req, encoded);
        return findByUsername(username);
    }

    public void delete(String username) {
        if (!repo.exists(username)) {
            throw new IllegalArgumentException(
                "Không tìm thấy user");
        }

        repo.delete(username);
    }
}
