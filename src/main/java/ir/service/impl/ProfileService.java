package ir.service.impl;

import ir.dto.ProfileUserDto;
import ir.dto.mapper.ProfileMapper;
import ir.model.entity.Profile;
import ir.model.entity.Role;
import ir.repository.ProfileRepository;
import ir.repository.UserRepository;
import ir.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ProfileService implements ir.service.ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final RoleService roleService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(ProfileRepository profileRepository, ProfileMapper profileMapper, RoleService roleService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
        this.roleService = roleService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

//    public void save(ProfileDto profileDto){
//        Person person = new Person (profileDto.getName(), profileDto.getFamily());
//        User user = new User (profileDto.getUsername(), profileDto.getPassword());
//
//        personService.save(person);
//
//        user.setPerson(person);
//        userService.save(user);
//
//        person.setUser(user);
//        personService.update(person);
//    }

    @Override
    public Profile save(Profile profile) {
        return profileRepository.save(profile);
    }

    @Transactional
    public Profile updateProfile(ProfileUserDto dto, Long profileId, boolean isAdmin) {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>profile service update started");
        Profile existingProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>> 1 existing profile : " + existingProfile);


        if (isAdmin) {
            // اگر ادمین بود، همه‌چیز به‌جز username قابل آپدیت
            profileMapper.updateEntity(dto, existingProfile);

            // نقش‌ها
            if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
                Set<Role> roles = dto.getRoles().stream()
                        .map(roleService::findByName)
                        .collect(Collectors.toSet());
                existingProfile.getUser().setRoleSet(roles);
            }

            // اگر پسورد وارد شده بود، رمزگذاری و آپدیت
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                existingProfile.getUser().setPassword(passwordEncoder.encode(dto.getPassword()));
            }
        }
        else {
            // اگر مشتری بود، فقط اطلاعات پروفایل خودش را تغییر دهد
            existingProfile.setFirstName(dto.getFirstName());
            existingProfile.setLastName(dto.getLastName());
            existingProfile.setEmail(dto.getEmail());
            existingProfile.setPhone(dto.getPhone());

            // اگر پسورد وارد شده بود، رمزگذاری و آپدیت
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                existingProfile.getUser().setPassword(passwordEncoder.encode(dto.getPassword()));
            }
        }

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>> dto : " + dto);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>> 4 existing profile : " + existingProfile);


        userRepository.save(existingProfile.getUser());
        return profileRepository.save(existingProfile);
    }


    @Override
    public void deleteById(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("پروفایل برای حذف وجود ندارد"));

        profile.setDeleted(true);
        profileRepository.save(profile);
    }

    @Override
    public Page<Profile> findAll(Pageable pageable) {
        return profileRepository.findAll(pageable);
    }

    @Override
    public Profile findById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));  //in ExceptionWrapper error's message will change
    }

    @Override
    public Page<Profile> findByOrderByFirstNameAsc(String firstName, Pageable pageable) {
        return profileRepository.findByOrderByFirstNameAsc(firstName, pageable);
    }

    @Override
    public Page<Profile> findByOrderByLastNameAsc(String lastName, Pageable pageable) {
        return profileRepository.findByOrderByLastNameAsc(lastName, pageable);
    }

    @Override
    public Profile findByUsername(String username) {
        return profileRepository.findByUserUsername(username);
    }
}
