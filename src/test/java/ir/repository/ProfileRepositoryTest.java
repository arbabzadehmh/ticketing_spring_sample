package ir.repository;

import ir.model.entity.Profile;
import ir.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProfileRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void shouldFindProfileByUsername() {

        User user = User.builder()
                .username("ali")
                .password("123")
                .build();

        userRepository.saveAndFlush(user);

        Profile profile = Profile.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .email("ali@test.com")
                .user(user)
                .build();

        profileRepository.save(profile);

        Profile result =
                profileRepository.findByUserUsername("ali");

        assertNotNull(result);
        assertEquals("Ali", result.getFirstName());
    }

    @Test
    void shouldFindEmailByUsername() {

        User user =
                User
                        .builder()
                        .username("akbar")
                        .password("123")
                        .build();

        userRepository.saveAndFlush(user);

        Profile profile =
                Profile
                        .builder()
                        .firstName("akbar")
                        .lastName("salehi")
                        .email("akbar@test.com")
                        .user(user)
                        .build();

        profileRepository.save(profile);

        String email =
                profileRepository.findEmailByUserUsername("akbar");

        assertEquals("akbar@test.com", email);
    }

    @Test
    void shouldFindProfilesByLastNameLike() {

        User user =
                User
                        .builder()
                        .username("reza")
                        .password("123")
                        .build();

        userRepository.saveAndFlush(user);

        profileRepository.save(
                Profile
                        .builder()
                        .firstName("reza")
                        .lastName("Ahmadi")
                        .user(user)
                        .build()
        );

        Page<Profile> result =
                profileRepository.findByLastNameLike(
                        "%Ahm%",
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldFindProfilesByUsernameLike() {

        User user =
                User
                        .builder()
                        .username("sara123")
                        .password("123")
                        .build();

        userRepository.saveAndFlush(user);

        profileRepository.save(
                Profile
                        .builder()
                        .firstName("sara")
                        .lastName("smith")
                        .user(user)
                        .build()
        );

        Page<Profile> result =
                profileRepository.findByUserUsernameLike(
                        "%sar%",
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
    }
}
