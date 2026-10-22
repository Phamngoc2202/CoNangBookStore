
package com.example.demo.controller;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.UserModel;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*"
)
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

  
    @GetMapping
public List<UserResponse> findAll() {
    try{
    final List<UserModel> users = service.findAll();
    return users
            .stream()
            .map(this::response)
            .collect(java.util.stream.Collectors.toList());
    } catch (Exception e) {
        return java.util.Collections.emptyList(); // Return an empty list in case of an error
}
}

private UserResponse response(UserModel u) {
    return new UserResponse(
            u.getUsername(),
            u.isStatus()
    );
}
   

    @GetMapping("/{username}")
    public UserResponse findOne(@PathVariable String username) {
      
            UserModel user = service.findByUsername(username);
            return response(user);
        
      
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody UserRequest req) {
       
            UserModel createdUser = service.create(req);
            return response(createdUser);
        
      
    }

    @PutMapping("/{username}")
    public UserResponse update(
            @PathVariable String username,
            @Valid @RequestBody UserRequest req) {
               
                    UserModel updatedUser = service.update(username, req);
                    return response(updatedUser);
               
      
    }

    @DeleteMapping("/{username}")
    public void delete(@PathVariable String username) {
       
            service.delete(username);
       
      
    }
}
