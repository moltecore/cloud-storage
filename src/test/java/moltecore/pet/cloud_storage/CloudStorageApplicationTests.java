package moltecore.pet.cloud_storage;


import moltecore.pet.cloud_storage.DTO.UserDTO;
import moltecore.pet.cloud_storage.models.User;
import moltecore.pet.cloud_storage.exceptions.ConflictException;
import moltecore.pet.cloud_storage.repositories.UsersRepositories;
import moltecore.pet.cloud_storage.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class AuthServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UsersRepositories usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        usersRepository.deleteAll();
    }

    @Test
    void createUser_shouldSaveUserInDatabase() {

        UserDTO dto = new UserDTO();

        dto.setUsername("test");
        dto.setPassword("123456");
        authService.saveUser(dto);

        User user = usersRepository.findByUsername("test").orElse(null);

        assertNotNull(user);
        assertEquals("test", user.getUsername());
    }

    @Test
    void createUser_duplicateUsername_shouldThrowException() {

        UserDTO dto = new UserDTO();

        dto.setUsername("test");
        dto.setPassword("123456");

        authService.saveUser(dto);
        assertThrows(ConflictException.class, () -> authService.saveUser(dto));
    }

    @Test
    void createUser_shouldEncodePassword() {

        UserDTO dto = new UserDTO();
        dto.setUsername("test");
        dto.setPassword("123456");

        authService.saveUser(dto);

        User user = usersRepository.findByUsername("test").orElseThrow();

        assertNotEquals("123456", user.getPassword());
        assertTrue(passwordEncoder.matches("123456", user.getPassword()));
    }

    @Test
    void createUser_shouldIncreaseUsersCount() {

        UserDTO user1 = new UserDTO();
        user1.setUsername("user1");
        user1.setPassword("111111");

        UserDTO user2 = new UserDTO();
        user2.setUsername("user2");
        user2.setPassword("222222");

        authService.saveUser(user1);
        authService.saveUser(user2);

        assertEquals(2, usersRepository.count());
    }

}
