package ir.controller.api;

import ir.controller.exception.ValidationException;
import ir.dto.ForgotPasswordDto;
import ir.dto.ProfileUserDto;
import ir.dto.ResetPasswordDto;
import ir.model.entity.Attachment;
import ir.model.entity.PasswordResetToken;
import ir.model.entity.Profile;
import ir.model.entity.User;
import ir.model.enums.FileType;
import ir.service.ProfileService;
import ir.service.UserService;
import ir.service.impl.AuthService;
import ir.service.impl.EntityLockService;
import ir.service.impl.PasswordResetService;
import ir.validation.OnCreate;
import ir.validation.OnUpdate;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/rest/profiles")
public class ProfileApi {

    private final ProfileService profileService;
    private final MessageSource messageSource;
    private final UserService userService;
    private final EntityLockService lockService;
    private final PasswordResetService resetService;
    private final AuthService authService;

    public ProfileApi(ProfileService profileService, MessageSource messageSource, UserService userService, PasswordEncoder passwordEncoder, EntityLockService lockService, PasswordResetService resetService, AuthService authService) {
        this.profileService = profileService;
        this.messageSource = messageSource;
        this.userService = userService;
        this.lockService = lockService;
        this.resetService = resetService;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
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

        Profile savedProfile = profileService.createProfileByCustomer(profileDto);

        String message = messageSource.getMessage("profiles.create.success", null, locale);

        return ResponseEntity.ok(Map.of(
                "id", savedProfile.getId(),
                "message", message
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDto dto, Locale locale) {

        authService.requestReset(dto.getUsername());

        String message = messageSource.getMessage("users.password.reset.email.sent", null, locale);

        return ResponseEntity.ok(
                Map.of("message", message)
        );
    }


//    @PostMapping("/reset-password/{username}")
//    public ResponseEntity<?> resetPassword(@PathVariable String username, Locale locale) {
//
//        String resetPassword = userService.resetPassword(username);
//
//        String message = messageSource.getMessage("users.password.reset.success", null, locale);
//
//        return ResponseEntity.ok(Map.of(
//                "resetPass", resetPassword,
//                "message", message
//        ));
//    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Validated(OnCreate.class) @RequestBody ResetPasswordDto dto,
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

        PasswordResetToken token = resetService.validateToken(dto.getToken());

        userService.changePassword(token.getUsername(), dto.getNewPassword());

        resetService.markAsUsed(token);

        String message = messageSource.getMessage("users.password.reset.success", null, locale);

        return ResponseEntity.ok(Map.of(
                "username", token.getUsername(),
                "message", message
        ));
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


        Profile savedProfile = profileService.createProfileByAdmin(profileDto);

        String message = messageSource.getMessage("profiles.create.success", null, locale);

        return ResponseEntity.ok(Map.of(
                "id", savedProfile.getId(),
                "message", message
        ));
    }

    @PostMapping("/{id}/edit-start")
    public ResponseEntity<?> startProfileEdit(@PathVariable Long id, Principal principal) {

        Profile profile = profileService.findById(id);

        profileService.validateProfileAccess(profile);

        lockService.lock("profile", id, principal.getName());
        return ResponseEntity.ok(Map.of("message", "locked"));
    }

    @PostMapping("/{id}/edit-stop")
    public void stopProfileEdit(@PathVariable Long id, Principal principal) {

        Profile profile = profileService.findById(id);

        profileService.validateProfileAccess(profile);

        lockService.unlock("profile", id, principal.getName());
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

    @PostMapping("/{profileId}/picture")
    public ResponseEntity<Profile> uploadProfilePicture(
            @PathVariable Long profileId,
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        Profile updated = profileService.uploadOrUpdateProfilePicture(profileId, file, principal.getName());
        return ResponseEntity.ok(updated);
    }

    // حذف عکس پروفایل
    @DeleteMapping("/{profileId}/picture")
    public ResponseEntity<Map<String, Object>> deleteProfilePicture(
            @PathVariable Long profileId,
            Locale locale
    ) {

        Profile updatedProfile = profileService.deleteProfilePicture(profileId);

        String message = messageSource.getMessage("profiles.picture.delete.success", null, locale);

        return ResponseEntity.ok(
                Map.of(
                        "message", message,
                        "version", updatedProfile.getVersion()
                )
        );
    }


    @GetMapping("/{profileId}/picture")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable Long profileId) {
        var pictureData = profileService.getProfilePictureBytes(profileId);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
        headers.setContentType(mapFileTypeToMediaType(pictureData.getSecond()));

        return new ResponseEntity<>(pictureData.getFirst(), headers, HttpStatus.OK);
    }

    private MediaType mapFileTypeToMediaType(FileType fileType) {
        if (fileType == null) return MediaType.APPLICATION_OCTET_STREAM;
        return switch (fileType) {
            case JPG -> MediaType.IMAGE_JPEG;
            case PNG -> MediaType.IMAGE_PNG;
            case BMP -> MediaType.valueOf("image/bmp");
            case PDF -> MediaType.APPLICATION_PDF;
            case TXT -> MediaType.TEXT_PLAIN;
        };
    }


}
