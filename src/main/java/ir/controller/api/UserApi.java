package ir.controller.api;


import ir.model.entity.Role;
import ir.model.entity.User;
import ir.service.RoleService;
import ir.service.UserService;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/rest/users")
public class UserApi {
    private final UserService userService;
    private final RoleService roleService;

    public UserApi(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public Object findAllUsers() {
        return userService.findAll();
    }

    @PostMapping
    public Object saveUser(@RequestBody User user) {
        Role role = Role.builder().name("ROLE_ADMIN").build();
        user.addRole(role);
        userService.save(user);
        return userService.findAll();
    }

//    @DeleteMapping("/{username}")
//    public Object deleteUser(@PathVariable String username) {
//        userService.delete(username);
//        return userService.findAll();
//    }

    @PutMapping("/{username}")
    public Object updateUser(@PathVariable String username, @RequestBody User user) {
        user.setUsername(username); // Ensure the username is updated
//        userService.update(user);
        return userService.findAll();
    }

}
