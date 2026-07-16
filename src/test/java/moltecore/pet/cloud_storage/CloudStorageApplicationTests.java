package moltecore.pet.cloud_storage;


import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class AuthServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16").withDatabaseName("CloudFileStorage").withUsername("postgres").withPassword("postgres");


    @Container
    static GenericContainer<?> minio =
            new GenericContainer<>("minio/minio")
                    .withExposedPorts(9000)
                    .withEnv("MINIO_ACCESS_KEY", "admin")
                    .withEnv("MINIO_SECRET_KEY", "password123")
                    .withCommand("server /data")
                    .waitingFor(Wait.forListeningPort());


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("minio.endpoint", () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));

        registry.add("minio.access-key", () -> "admin");

        registry.add("minio.secret-key", () -> "password123");

        registry.add("minio.bucket", () -> "user-files");
    }


    @Autowired
    private AuthService authService;

    @Autowired
    private UsersRepositories usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MinioClient minioClient;


    @BeforeEach
    void prepare() throws Exception {

        usersRepository.deleteAll();

        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket("user-files").build());

        if (!bucketExists) {
            minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket("user-files").build());
        }
    }


    @Test
    void createUser_shouldSaveUserInDatabase() {

        UserDTO dto = new UserDTO();
        dto.setUsername("test");
        dto.setPassword("123456");

        authService.saveUser(dto);

        User user = usersRepository.findByUsername("test").orElseThrow();

        assertEquals("test", user.getUsername());
    }


    @Test
    void createUser_shouldCreateUserDirectoryInMinio() throws Exception {

        UserDTO dto = new UserDTO();

        dto.setUsername("test");
        dto.setPassword("123456");

        authService.saveUser(dto);

        User user = usersRepository.findByUsername("test").orElseThrow();

        assertDoesNotThrow(() -> minioClient.statObject(

                StatObjectArgs.builder()
                        .bucket("user-files")
                        .object("user-" + user.getId() + "-files/")
                        .build()
                )
        );
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
