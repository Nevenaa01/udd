package com.example.udd.controller;

import com.example.udd.dto.AccessToken;
import com.example.udd.dto.CredentialDto;
import com.example.udd.dto.RegistrationDto;
import com.example.udd.security.jwt.JwtUtils;
import com.example.udd.security.services.UserDetailsImpl;
import com.example.udd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody CredentialDto loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        String jwtSource = jwtCookie.toString().split("=")[1].split(";")[0];
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtSource)
                .body(new AccessToken(userDetails.getId(), jwtSource));
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegistrationDto registrationDto) {
        String registrationStatus = userService.registerUser(registrationDto);
        if (registrationStatus.equals("Registration successful!")) {
            return ResponseEntity.ok(registrationStatus);
        }
        return ResponseEntity.badRequest().body(registrationStatus);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("You've been signed out!");
    }
}
