package moltecore.pet.cloud_storage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import moltecore.pet.cloud_storage.DTO.DownloadDTO;
import moltecore.pet.cloud_storage.DTO.ResourceDTO;
import moltecore.pet.cloud_storage.security.UsersDetails;
import moltecore.pet.cloud_storage.service.ResourceService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@Tag(name = "Resources", description = "Работа с файлами и папками")
@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    private final ResourceService resourceService;

    @Autowired
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Operation(summary = "Загрузить ресурс", description = "Загружает файл или папку в хранилище")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ресурс успешно загружен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @PostMapping()
    public ResponseEntity<?> upload(@RequestParam("path") String path, @RequestParam("object") List<MultipartFile> files, @AuthenticationPrincipal UsersDetails userDetails
    ) {

        ResourceDTO response = resourceService.upload(userDetails.getId(), path, files);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получение информации о ресурсе", description = "Показывает информацию о файле или папке: размер, путь")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Информация получена"),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping()
    public ResponseEntity<?> getInfo(@RequestParam ("path") String path, @AuthenticationPrincipal UsersDetails userDetails) {

        ResourceDTO response = resourceService.getResource(userDetails.getId(), path);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Удалить ресурс", description = "Удаляет файл или папку из хранилища")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ресурс удален"),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @DeleteMapping()
    public ResponseEntity<?> delete(@RequestParam ("path") String path, @AuthenticationPrincipal UsersDetails userDetails) {

        resourceService.delete(userDetails.getId(), path);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Удалилось успешно");
    }


    @Operation(summary = "Найти ресурс", description = "Находит файл или папку в облачном хранилище")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Результаты поиска получены"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam ("query") String query, @AuthenticationPrincipal UsersDetails userDetails) {

        List<ResourceDTO> response = resourceService.search(userDetails.getId(), query);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Скачать ресурс", description = "Скачивает файл или папку пользователя из хранилища")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Файл успешно скачан"),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping("/download")
    public ResponseEntity<@NotNull StreamingResponseBody> download(
            @RequestParam("path") String path,
            @AuthenticationPrincipal UsersDetails userDetails
    ) {

        DownloadDTO downloadDTO = resourceService.download(userDetails.getId(), path);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + downloadDTO.fileName() + "\""
                )
                .body(downloadDTO.body());
    }

    @Operation(summary = "Переместить ресурс", description = "Перемещает файл или папку по хранилищу")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ресурс перемещен"),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @PostMapping("/move")
    public ResponseEntity<?> move(@RequestParam("from") String from, @RequestParam("to") String to, @AuthenticationPrincipal UsersDetails userDetails) {

        ResourceDTO response = resourceService.moveResource(userDetails.getId(), from, to);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
