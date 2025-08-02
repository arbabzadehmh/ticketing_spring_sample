package ir.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/customers")
public class CustomerController {

    @GetMapping
    public String loadCustomerPage(Model model)
    {
        return "customer";
    }
}
