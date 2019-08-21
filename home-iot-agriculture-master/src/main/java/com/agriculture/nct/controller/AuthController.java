package com.agriculture.nct.controller;

import com.agriculture.nct.database.DBWeb;
import com.agriculture.nct.model.Role;
import com.agriculture.nct.model.User;
import com.agriculture.nct.exception.AppException;
import com.agriculture.nct.payload.response.ApiResponse;
import com.agriculture.nct.payload.response.JwtAuthenticationResponse;
import com.agriculture.nct.payload.request.LoginRequest;
import com.agriculture.nct.payload.request.SignUpRequest;
import com.agriculture.nct.security.JwtTokenProvider;
import com.agriculture.nct.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final DBWeb dbWeb;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider tokenProvider;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, DBWeb dbWeb, PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.dbWeb = dbWeb;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        dbWeb.updateLastLogin(userPrincipal.getId());
        String jwt = tokenProvider.generateUserToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (dbWeb.getUserByUsername(signUpRequest.getUsername()).isPresent())
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Username is already taken!"));

        if (dbWeb.getUserByEmail(signUpRequest.getEmail()).isPresent())
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Email Address already in use!"));

        // Creating user's account
        User user = new User(signUpRequest.getUsername(), signUpRequest.getFullName(), signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()), Role.ROLE_USER.getId());

        int userId = dbWeb.addUser(user);

        if (userId == 0) throw new AppException("User registered failed!");

        User createdUser = dbWeb.getUserById(userId).orElseThrow(() -> new AppException("Add user failed."));

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(createdUser.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }
}