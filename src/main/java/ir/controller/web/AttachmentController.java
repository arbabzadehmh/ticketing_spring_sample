package ir.controller.web;


import ir.model.entity.Attachment;
import ir.service.AttachmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/attachment")
public class AttachmentController {
    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping
    public String loadAttachmentPage(Model model) {
        model.addAttribute("attachment", new Attachment());
        model.addAttribute("attachmentList", attachmentService.findAll());

        return "attachment";
    }

    @PostMapping
    public String save(Model model, Attachment attachment) {
        attachmentService.save(attachment);

        return "redirect:attachment";

    }
}