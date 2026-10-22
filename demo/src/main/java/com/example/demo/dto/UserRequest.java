package com.example.demo.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank(message = "Username không được để trống")
    @Size(max = 100, message = "Username tối đa 100 ký tự")
    String username,

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, max = 255,
          message = "Password phải từ 8 đến 255 ký tự")
    String password,

    Boolean status
) {}
