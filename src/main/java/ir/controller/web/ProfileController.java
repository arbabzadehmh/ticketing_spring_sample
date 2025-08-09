package ir.controller.web;


import ir.controller.exception.ValidationException;
import ir.dto.ProfileUserDto;
import ir.dto.mapper.ProfileMapper;
import ir.model.entity.Profile;
import ir.service.RoleService;
import ir.service.UserService;
import ir.service.ProfileService;
import ir.validation.OnCreate;
import ir.validation.OnUpdate;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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
    private final UserService userService;
    private final MessageSource messageSource;

    public ProfileController(ProfileService profileService, UserService userService, RoleService roleService, ProfileMapper profileMapper, MessageSource messageSource) {
        this.profileService = profileService;
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String profilesList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String username,
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

            Page<Profile> profiles;
            if (lastName != null && !lastName.isEmpty()) {
                profiles = profileService.findByLastNameLike(lastName, pageable);
            } else if (username != null && !username.isEmpty()) {
                profiles = profileService.findByUserUsernameLike(username, pageable);
            } else {
                profiles = profileService.findAll(pageable);
            }

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


    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> saveProfile(
            @Validated(OnCreate.class) @RequestBody ProfileUserDto profileDto,
            BindingResult bindingResult,
            Locale locale
    ) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            throw new ValidationException(errors);
        }

        profileService.createProfileByCustomer(profileDto);

        String message = messageSource.getMessage("profiles.create.success", null, locale);

        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/admin/create-profile")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @ResponseBody
    public ResponseEntity<?> createProfileByAdmin(
            @Validated(OnCreate.class) @RequestBody ProfileUserDto profileDto,
            BindingResult bindingResult,
            Locale locale
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            throw new ValidationException(errors);
        }

        if ("error".equalsIgnoreCase(profileDto.getFirstName())) {
            throw new RuntimeException("شبیه‌سازی خطای سرور!");
        }

        profileService.createProfileByAdmin(profileDto);

        String message = messageSource.getMessage("profiles.create.success", null, locale);

        return ResponseEntity.ok(Map.of("message", message));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @ResponseBody
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @Validated(OnUpdate.class) @RequestBody ProfileUserDto profileUserDto,
            BindingResult bindingResult,
            Authentication authentication,
            Locale locale
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        boolean isAdminOrManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));

        profileService.updateProfile(profileUserDto, id, isAdminOrManager);

        String message = messageSource.getMessage("profiles.edit.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @ResponseBody
    public ResponseEntity<?> deleteProfile(@PathVariable Long id, Locale locale) {
        Profile profile = profileService.findById(id);
        profileService.deleteById(id);
        userService.deleteByUsername(profile.getUser().getUsername());
        String message = messageSource.getMessage("profiles.delete.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }

}
