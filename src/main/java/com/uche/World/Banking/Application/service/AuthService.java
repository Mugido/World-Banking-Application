package com.uche.World.Banking.Application.service;

import com.uche.World.Banking.Application.payload.request.LoginRequest;
import com.uche.World.Banking.Application.payload.request.UserRequest;
import com.uche.World.Banking.Application.payload.response.ApiResponse;
import com.uche.World.Banking.Application.payload.response.BankResponse;
import com.uche.World.Banking.Application.payload.response.JwtAuthResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    BankResponse registerUser(UserRequest userRequest);

    ResponseEntity<ApiResponse<JwtAuthResponse>> loginUser(LoginRequest loginRequest);
}
