package ir.controller.api;

import ir.controller.exception.ValidationException;
import ir.dto.ProfileUserDto;
import ir.model.entity.Profile;
import ir.service.ProfileService;
import ir.service.UserService;
import ir.validation.OnCreate;
import ir.validation.OnUpdate;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/rest/profiles")
public class ProfileApi {

    private final ProfileService profileService;
    private final MessageSource messageSource;
    private final UserService userService;

    public ProfileApi(ProfileService profileService, MessageSource messageSource, UserService userService) {
        this.profileService = profileService;
        this.messageSource = messageSource;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String username
    ) {

        if (size <= 0) size = 10;
        if (!Arrays.asList("firstName", "lastName").contains(sortBy)) {
            sortBy = "firstName";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());


        Page<Profile> profiles;

        if (lastName != null && !lastName.isEmpty()) {
            profiles = profileService.findByLastNameLike(lastName, pageable);
        } else if (username != null && !username.isEmpty()) {
            profiles = profileService.findByUserUsernameLike(username, pageable);
        } else {
            profiles = profileService.findAll(pageable);
        }

        return ResponseEntity.ok(profiles);
    }

    @PostMapping("/register")
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

    @PostMapping("/create-profile")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
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
    public ResponseEntity<?> deleteProfile(@PathVariable Long id, Locale locale) {
        Profile profile = profileService.findById(id);
        profileService.deleteById(id);
        userService.deleteByUsername(profile.getUser().getUsername());
        String message = messageSource.getMessage("profiles.delete.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
