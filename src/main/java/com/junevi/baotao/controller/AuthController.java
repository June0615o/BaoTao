package com.junevi.baotao.controller;

import com.junevi.baotao.domain.User;
import com.junevi.baotao.dto.AuthDtos;
import com.junevi.baotao.security.JwtUtil;
import com.junevi.baotao.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        if (userService.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }
        if (userService.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("邮箱已存在");
        }
        String encoded = passwordEncoder.encode(request.getPassword());
        User user = userService.createCustomer(request.getUsername(), request.getEmail(), encoded);
        AuthDtos.AuthResponse resp = new AuthDtos.AuthResponse();
        resp.setUsername(user.getUsername());
        resp.setRole(user.getRole().name());
        resp.setToken(JwtUtil.generateToken(user.getUsername(), user.getRole().name()));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        String role = principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        AuthDtos.AuthResponse resp = new AuthDtos.AuthResponse();
        resp.setUsername(principal.getUsername());
        resp.setRole(role);
        resp.setToken(JwtUtil.generateToken(principal.getUsername(), role));
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok().build();
        }
        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        String role = principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        AuthDtos.AuthResponse resp = new AuthDtos.AuthResponse();
        resp.setUsername(principal.getUsername());
        resp.setRole(role);
        return ResponseEntity.ok(resp);
    }
}


