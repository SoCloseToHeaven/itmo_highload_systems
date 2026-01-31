package ru.ifmo.highload.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.ifmo.highload.auth.impl.user.UserRepository;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(u -> new User(
                        u.getUsername(),
                        u.getPasswordHash(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
    }
}
