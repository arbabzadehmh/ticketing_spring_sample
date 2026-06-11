package ir.service.impl;

import ir.controller.exception.DuplicateUsernameException;
import ir.model.entity.Role;
import ir.model.entity.User;
import ir.repository.RoleRepository;
import ir.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void save_shouldThrowDuplicateUsernameException() {

        User user = new User();
        user.setUsername("admin");

        when(userRepository.existsUserByUsername("admin"))
                .thenReturn(true);

        assertThrows(
                DuplicateUsernameException.class,
                () -> userService.save(user)
        );
    }

    @Test
    void save_shouldEncodePassword() {

        User user = new User();

        user.setUsername("admin");
        user.setPassword("1234");

        Role role = new Role();
        role.setName("ROLE_ADMIN");

        user.setRoleSet(Set.of(role));

        when(userRepository.existsUserByUsername("admin"))
                .thenReturn(false);

        when(passwordEncoder.encode("1234"))
                .thenReturn("ENCODED");

        when(roleRepository.findByName("ROLE_ADMIN"))
                .thenReturn(Optional.of(role));

        when(userRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        User saved = userService.save(user);

        assertEquals(
                "ENCODED",
                saved.getPassword()
        );
    }

    @Test
    void save_shouldThrowWhenRoleNotFound() {

        User user = new User();

        user.setUsername("admin");
        user.setPassword("1234");

        Role role = new Role();
        role.setName("ROLE_ADMIN");

        user.setRoleSet(Set.of(role));

        when(userRepository.existsUserByUsername("admin"))
                .thenReturn(false);

        when(roleRepository.findByName("ROLE_ADMIN"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> userService.save(user)
        );
    }

    @Test
    void changePassword_shouldUpdatePassword() {

        User user = new User();

        user.setUsername("admin");

        when(userRepository.findById("admin"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode("newPass"))
                .thenReturn("ENCODED");

        userService.changePassword("admin", "newPass");

        assertEquals("ENCODED", user.getPassword());

        verify(userRepository).save(user);
    }

    @Test
    void changePassword_shouldThrowEntityNotFoundException() {

        when(userRepository.findById("admin"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.changePassword("admin", "newPass"));
    }
}
