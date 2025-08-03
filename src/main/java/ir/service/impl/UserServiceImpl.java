package ir.service.impl;


import ir.controller.exception.DuplicateUsernameException;
import ir.model.entity.Role;
import ir.model.entity.User;
import ir.repository.RoleRepository;
import ir.repository.UserRepository;
import ir.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User save(User user) {

        if (userRepository.existsUserByUsername(user.getUsername())) {
            throw new DuplicateUsernameException();
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roles = user.getRoleSet().stream()
                .map(role -> roleRepository.findByName(role.getName())
                        .orElseThrow(() -> new EntityNotFoundException("Role not found: " + role.getName())))
                .collect(Collectors.toSet());
        user.setRoleSet(roles);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        user.setCredentialsExpiryDate(LocalDateTime.now().plusMonths(6));
        User saved = userRepository.save(user);
        logger.info("User saved with ID: {}", saved.getUsername());
        return saved;
    }

    @Transactional
    public User edit(User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        logger.debug("Fetching all users from database");
        List<User> users = userRepository.findAll();
        logger.debug("Retrieved {} users", users.size());
        return users;
    }

    @Transactional
    public void saveUserWithRollbackDemo(User user) {
        logger.info("Starting transaction to save user: {}", user.getUsername());
        userRepository.save(user);
        logger.info("User saved, now simulating failure");
        throw new RuntimeException("Simulating transaction rollback");
    }

//    @Transactional
//    public User update(User updatedUser) {
//
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>user service update started");
//
//
//        User existingUser = userRepository.findByUsername(updatedUser.getUsername())
//                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + updatedUser.getUsername()));
//
//        // جلوگیری از تغییر username
//        updatedUser.setUsername(existingUser.getUsername());
//
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>user service: " + updatedUser.getUsername() + updatedUser.getPassword());
//
//        // اگر پسورد جدید ارسال شد
//        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
//            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
//        }
//
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>user service: " +existingUser.getPassword());
//
//
//
//        // نقش‌ها را دوباره بارگذاری کن
//        if (updatedUser.getRoleSet() != null && !updatedUser.getRoleSet().isEmpty()) {
//            Set<Role> roles = updatedUser.getRoleSet().stream()
//                    .map(role -> roleRepository.findByName(role.getName())
//                            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + role.getName())))
//                    .collect(Collectors.toSet());
//            existingUser.setRoleSet(roles);
//        }
//
//        return userRepository.save(existingUser);
//    }


    @Override
    public void deleteByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findById(username).orElse(null);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsById(username);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findByRole(Role role) {
        return userRepository.findByRoleSetContaining(role);
    }

    @Override
    public List<User> findByRoleName(String roleName) {
        return userRepository.findByRoleSetName(roleName);
    }

    @Override
    public User findByUsernameAndPassword(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password).orElse(null);
    }
}
