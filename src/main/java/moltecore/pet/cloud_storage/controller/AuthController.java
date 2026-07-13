package moltecore.pet.cloud_storage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import moltecore.pet.cloud_storage.DTO.UserDTO;
import moltecore.pet.cloud_storage.service.AuthService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Authentication", description = "Авторизация пользователей")
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Регистрация пользователя", description = "Регистрирует пользователя, сохраняет его логин и пароль в БД")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь зарегистрирован"),
            @ApiResponse(responseCode = "409", description = "Пользователь уже существует"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    @PostMapping("/auth/sign-up")
    public ResponseEntity<?> create(@RequestBody @Valid UserDTO userDTO) {

        authService.register(userDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("username", userDTO.getUsername()));
    }

    @Operation(summary = "Логин пользователя", description = "Позволяет пользователю войти, используя логин и пароль")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Авторизация успешна"),
            @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    })
    @PostMapping("/auth/sign-in")
    public ResponseEntity<?> login(@RequestBody @Valid UserDTO userDTO) {

        authService.login(userDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("username", userDTO.getUsername()));
    }

    @Operation(summary = "Получить информацию о пользователе", description = "Выводит имя текущего пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Информация получена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(Map.of("username", authentication.getName()));
    }

    @Operation(summary = "Логаут пользователя", description = "Выход пользователя из приложения")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Выход выполнен успешно")
    })
    @PostMapping("/auth/sign-out")
    public ResponseEntity<@NotNull Void> logout(HttpServletRequest request) {

        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

}