package moltecore.pet.cloud_storage.service.storage;

import lombok.RequiredArgsConstructor;
import moltecore.pet.cloud_storage.DTO.ResourceDTO;
import moltecore.pet.cloud_storage.exceptions.BadRequestException;
import moltecore.pet.cloud_storage.exceptions.ConflictException;
import moltecore.pet.cloud_storage.exceptions.NotFoundException;
import moltecore.pet.cloud_storage.service.interfaces.StorageService;
import moltecore.pet.cloud_storage.util.StoragePathUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final StorageService storageService;


    public List<ResourceDTO> upload(int id, String path, List<MultipartFile> files) {

        List<ResourceDTO> uploadedResources = new ArrayList<>();
        String storagePath = StoragePathUtils.buildStoragePath(id, path);

        for (MultipartFile file : files) {


            String fileName = file.getOriginalFilename();

            if (fileName == null || fileName.isBlank()) {
                continue;
            }

            String objectPath = storagePath + fileName;

            if (storageService.isResourceExist(objectPath)) {
                throw new ConflictException("Ресурс " + fileName + " уже существует");
            }

            storageService.uploadFile(objectPath, file);
            int index = fileName.lastIndexOf("/");

            if (index == -1) {

                uploadedResources.add(
                        new ResourceDTO(
                        StoragePathUtils.normalizeDirectoryPath(path),
                        fileName,
                        file.getSize(),
                        "FILE"));
            } else {

                uploadedResources.add(
                        new ResourceDTO(
                        StoragePathUtils.normalizeDirectoryPath(path + fileName.substring(0, index + 1)),
                        fileName.substring(index + 1),
                        file.getSize(),
                        "FILE"));
            }
        }

        return uploadedResources;
    }

    public ResourceDTO createDirectory(int id, String path) {

        if (!path.endsWith("/")) {
            throw new BadRequestException("Путь папки должен заканчиваться на /");
        }

        String objectPath = StoragePathUtils.buildStoragePath(id, path);
        String parentPath = StoragePathUtils.getParent(objectPath);

        if (!storageService.isDirectoryExist(parentPath)) {
            throw new NotFoundException("Родительская папка не существует");
        }

        if (storageService.isDirectoryExist(objectPath)) {
            throw new ConflictException("Папка уже существует");
        }

        storageService.createDirectory(objectPath);

        return new ResourceDTO(
                StoragePathUtils.getParent(path),
                StoragePathUtils.getName(path),
                null,
                "DIRECTORY"
        );
    }
}

