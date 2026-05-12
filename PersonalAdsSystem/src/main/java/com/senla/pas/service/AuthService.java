package com.senla.pas.service;

import com.senla.pas.dao.RoleDao;
import com.senla.pas.dao.UserDao;
import com.senla.pas.dto.request.LoginRequest;
import com.senla.pas.dto.request.RegisterRequest;
import com.senla.pas.dto.response.AuthResponse;
import com.senla.pas.dto.response.UserResponse;
import com.senla.pas.entity.Role;
import com.senla.pas.entity.User;
import com.senla.pas.exception.ForbiddenException;
import com.senla.pas.exception.PasException;
import com.senla.pas.exception.ResourceAlreadyExistsException;
import com.senla.pas.mapper.UserMapper;
import com.senla.pas.security.JwtTokenProvider;
import com.senla.pas.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private PasswordEncoder passwordEncoder;
    private UserDao userDao;
    private RoleDao roleDao;
    private UserMapper userMapper;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserDao userDao, PasswordEncoder passwordEncoder, RoleDao roleDao, UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.roleDao = roleDao;
        this.userMapper = userMapper;
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        return register(request, "ROLE_USER");
    }

    @Transactional
    public AuthResponse registerAdmin(RegisterRequest request) {
        return register(request, "ROLE_ADMIN");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        User user = userDao.findByUsernameOrEmail(request.getUsernameOrEmail()).orElseThrow(() -> new PasException("Пользователь с таким именем или почтой не найден"));

        String jwtToken = jwtTokenProvider.generateToken(authentication, user.getId());

        UserResponse userResponse = userMapper.toResponse(user);

        return new AuthResponse(jwtToken, userResponse);
    }

    private AuthResponse register(RegisterRequest request, String role) {
        if (userDao.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException(
                    "Пользователь с именем '" + request.getUsername() + "' уже существует"
            );
        }

        if (userDao.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "Пользователь с такой почтой уже существует"
            );
        }

        Role userRole = roleDao.findByName(role)
                .orElseThrow(() -> new PasException("Роль не найдена: " + role));

        User user = new User(request.getUsername(), passwordEncoder.encode(request.getPassword()), request.getEmail());

        user.addRole(userRole);
        user.setCreatedAt(LocalDateTime.now());

        userDao.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String jwtToken = jwtTokenProvider.generateToken(authentication, user.getId());
        UserResponse userResponse = userMapper.toResponse(user);

        return new AuthResponse(jwtToken, userResponse);
    }
}
