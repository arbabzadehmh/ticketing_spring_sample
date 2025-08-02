package ir.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Slf4j
@Controller
@RequestMapping("/admins")
public class AdminController {

    @GetMapping
    public String loadAdminPage(Model model, Principal principal)
    {
        return "admin";
    }
}
