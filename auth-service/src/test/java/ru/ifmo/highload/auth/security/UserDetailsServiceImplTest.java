package ru.ifmo.highload.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.ifmo.highload.auth.impl.user.Role;
import ru.ifmo.highload.auth.impl.user.User;
import ru.ifmo.highload.auth.impl.user.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl unit tests")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("cashier1");
        user.setPasswordHash("$2a$10$hash");
        user.setRole(Role.CASHIER);
    }

    @Test
    @DisplayName("loadUserByUsername returns UserDetails with correct authorities")
    void loadUserByUsername_success() {
        when(userRepository.findByUsername("cashier1")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("cashier1");

        assertThat(details.getUsername()).isEqualTo("cashier1");
        assertThat(details.getPassword()).isEqualTo("$2a$10$hash");
        assertThat(details.getAuthorities()).hasSize(1);
        assertThat(details.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_CASHIER");
    }

    @Test
    @DisplayName("loadUserByUsername throws when user not found")
    void loadUserByUsername_notFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Пользователь не найден: unknown");
    }

    @Test
    @DisplayName("loadUserByUsername maps SUPERVISOR role correctly")
    void loadUserByUsername_supervisorRole() {
        user.setRole(Role.SUPERVISOR);
        when(userRepository.findByUsername("super")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("super");

        assertThat(details.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_SUPERVISOR");
    }
}
