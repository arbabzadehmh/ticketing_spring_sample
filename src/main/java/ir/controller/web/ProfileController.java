package ir.controller.web;


import ir.controller.exception.ValidationException;
import ir.dto.ProfileUserDto;
import ir.dto.mapper.ProfileMapper;
import ir.model.entity.Profile;
import ir.service.RoleService;
import ir.service.UserService;
import ir.service.ProfileService;
import ir.service.impl.FileStorageService;
import ir.validation.OnCreate;
import ir.validation.OnUpdate;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.*;


@Controller
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final MessageSource messageSource;

    public ProfileController(ProfileService profileService, UserService userService, RoleService roleService, ProfileMapper profileMapper, FileStorageService fileStorageService, MessageSource messageSource) {
        this.profileService = profileService;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String profilesList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(required = false) Boolean fragment,
            Model model,
            Authentication authentication
    ) {
        // اعتبارسنجی پارامترها
        if (size <= 0) size = 10;
        if (!Arrays.asList("firstName", "lastName").contains(sortBy)) {
            sortBy = "firstName";
        }

        // ایجاد صفحه‌بندی با مرتب‌سازی
        Sort sort = Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        boolean isAdminOrManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));

        model.addAttribute("isAdmin", isAdminOrManager);

        if (isAdminOrManager) {

            Page<Profile> profiles = profileService.findAll(pageable);

            model.addAttribute("profiles", profiles);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", profiles.getTotalPages());
        } else {
            // مشتری → فقط پروفایل خودش
            String myUsername = authentication.getName();
            Profile profile = profileService.findByUsername(myUsername);
            model.addAttribute("profile", profile);
        }

        return fragment != null && fragment ?
                "fragments/profile-fragments/profiles-table :: profiles-table" :
                "profile";
    }

    @GetMapping("/{id}/picture")
    @ResponseBody
    public ResponseEntity<Resource> getProfilePicture(@PathVariable Long id) {
        Profile profile = profileService.findById(id);

        profileService.validateProfileAccess(profile);

        if (profile.getProfilePicture() == null) return ResponseEntity.notFound().build();
        GridFsResource res = fileStorageService.getResource(profile.getProfilePicture().getMongoFileId());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // یا نوع واقعی عکس
                .body(res);
    }

    @GetMapping("/{id}/card")
    public String getProfileCard(@PathVariable Long id, Model model) {
        Profile profile = profileService.findById(id);

        profileService.validateProfileAccess(profile);

        model.addAttribute("profile", profile);
        return "fragments/profile-fragments/profile-card :: profile-card";
    }

}
