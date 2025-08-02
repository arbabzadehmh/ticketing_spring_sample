package ir.controller.web;

import ir.model.entity.Role;
import ir.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }


    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("role", new Role());
        model.addAttribute("roleList", roleService.findAll());
        return "role";
    }

    @PostMapping
    public String saveRole(Role role) {
        try{
            roleService.save(role);
            log.info("Role Saved");
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        return "redirect:/roles";
    }

    @DeleteMapping(path = "/{name}")
    public String removeRole(@PathVariable("name") String roleName) {
        try{
            roleService.delete(roleName);
            log.info("Role Removed");
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        return "redirect:/roles";
    }
}
