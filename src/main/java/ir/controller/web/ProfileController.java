package ir.controller.web;


import ir.controller.exception.ValidationException;
import ir.dto.ProfileUserDto;
import ir.dto.mapper.ProfileMapper;
import ir.model.entity.Profile;
import ir.model.entity.Role;
import ir.model.entity.User;
import ir.service.RoleService;
import ir.service.UserService;
import ir.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;
    private final RoleService roleService;
    private final ProfileMapper profileMapper;

    public ProfileController(ProfileService profileService, UserService userService, RoleService roleService, ProfileMapper profileMapper) {
        this.profileService = profileService;
        this.userService = userService;
        this.roleService = roleService;
        this.profileMapper = profileMapper;
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

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> isAdmin: " + isAdmin);
        model.addAttribute("isAdmin", isAdmin);

        if (isAdmin) {
            // ادمین → همه پروفایل‌ها
            Page<Profile> profiles = profileService.findAll(pageable);
            model.addAttribute("profiles", profiles);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", profiles.getTotalPages());
        } else {
            // مشتری → فقط پروفایل خودش
            String username = authentication.getName();
            Profile profile = profileService.findByUsername(username);
            model.addAttribute("profile", profile);
        }

        return fragment != null && fragment ?
                "fragments/profile-fragments/profiles-table :: profiles-table" :
                "profile";
    }


    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> saveProfile(
            @Valid @RequestBody ProfileUserDto profileDto,
            BindingResult bindingResult
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

        Role customerRole = roleService.findByName("ROLE_CUSTOMER");
        User user =
                User
                        .builder()
                        .username(profileDto.getUsername())
                        .password(profileDto.getPassword())
                        .roleSet(Set.of(customerRole))
                        .build();

        userService.save(user);

        Profile profile = profileMapper.toEntity(profileDto);

        profile.setUser(user);
        profileService.save(profile);

        return ResponseEntity.ok(profile);
    }

    @PostMapping("/admin/create-profile")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProfileByAdmin(
            @Valid @RequestBody ProfileUserDto profileDto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        Set<Role> roles = profileDto.getRoles().stream()
                .map(roleService::findByName)
                .collect(Collectors.toSet());

        User user = User.builder()
                .username(profileDto.getUsername())
                .password(profileDto.getPassword())
                .roleSet(roles)
                .build();

        userService.save(user);

        Profile profile = profileMapper.toEntity(profileDto);
        profile.setUser(user);
        profileService.save(profile);

        return ResponseEntity.ok(Map.of("message", "پروفایل با نقش‌های انتخابی ایجاد شد"));
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody ProfileUserDto profileUserDto,
            BindingResult bindingResult,
            Authentication authentication
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }


        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        profileService.updateProfile(profileUserDto, id, isAdmin);

        return ResponseEntity.ok(Map.of("message", "پروفایل با موفقیت ویرایش شد"));
    }


    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteProfile(@PathVariable Long id) {
        Profile profile = profileService.findById(id);
        profileService.deleteById(id);
        userService.deleteByUsername(profile.getUser().getUsername());
        return ResponseEntity.ok().build();
    }

}
