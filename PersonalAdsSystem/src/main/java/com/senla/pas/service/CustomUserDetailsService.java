package com.senla.pas.service;

import com.senla.pas.dao.UserDao;
import com.senla.pas.entity.User;
import com.senla.pas.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private UserDao userDao;

    @Autowired
    public CustomUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userDao.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + usernameOrEmail));

        Set<String> authorityNames = new LinkedHashSet<>();

        user.getRoles().forEach(role -> authorityNames.add(role.getName()));
        user.getRoles().stream()
                .flatMap(role -> role.getPrivileges().stream())
                .map(privilege -> privilege.getName())
                .forEach(authorityNames::add);

        List<SimpleGrantedAuthority> authorities = authorityNames.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
