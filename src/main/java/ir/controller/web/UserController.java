package ir.controller.web;

import ir.model.entity.Role;
import ir.model.entity.User;
import ir.service.RoleService;
import ir.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
@Slf4j
public class UserController
{
    private final UserService userService;
    private final RoleService roleService;

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String showForm(Model model)
    {
        model.addAttribute("user", new User());
        model.addAttribute("roleList", roleService.findAll());
        model.addAttribute("userList", userService.findAll());
        return "user2";
    }

    @PostMapping()
    public String saveUser(User user, @ModelAttribute("roleName")String roleName)
     {
        Role role = roleService.findByName(roleName);
        user.addRole(role);
        userService.save(user);
        log.info("User Saved...!");
        return "redirect:users";
    }

//    @PutMapping()
//    public String update(User user)
//    {
//        userService.update(user);
//        log.info("User Updated...!");
//        return "redirect:users";
//    }

    @GetMapping("/{roleName}")
    public String findByRoleName(Model model, String roleName)
    {
        List<User> userList = userService.findByRoleName(roleName);
        model.addAttribute("userList", userList);
        return "redirect:users";
    }


//    @DeleteMapping("/{username}")
//    public String delete(@PathVariable("username") String username)
//    {
//        userService.delete(username);
//        log.info("User Deleted...!");
//        return "redirect:users";
//    }

}