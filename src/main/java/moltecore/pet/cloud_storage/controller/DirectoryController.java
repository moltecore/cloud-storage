package moltecore.pet.cloud_storage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import moltecore.pet.cloud_storage.DTO.ResourceDTO;
import moltecore.pet.cloud_storage.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import moltecore.pet.cloud_storage.security.UsersDetails;

import java.util.List;

@Tag(name = "Directory", description = "Работа с папками")
@RestController
@RequestMapping("/api/directory")
public class DirectoryController {

    private final ResourceService resourceService;

    @Autowired
    public DirectoryController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Operation(summary = "Создать папку", description = "Создает пустую папку в хранилище")
    @ApiResponses( {
            @ApiResponse(responseCode = "201", description = "Папка успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректный путь"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @PostMapping
    public ResponseEntity<?> uploadDirectory(@RequestParam("path") String path, @AuthenticationPrincipal UsersDetails userDetails) {


        ResourceDTO response = resourceService.createFolder(userDetails.getId(), path);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить содержимое папки", description = "Получает содержимое папки, находящейся в хранилище")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Содержимое папки получено"),
            @ApiResponse(responseCode = "404", description = "Папка не найдена"),
             @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping()
    public ResponseEntity<?> getDirectory(@RequestParam("path") String path, @AuthenticationPrincipal UsersDetails userDetails) {

        List<ResourceDTO> response = resourceService.getContent(userDetails.getId(), path);
        return ResponseEntity.ok(response);

    }
}