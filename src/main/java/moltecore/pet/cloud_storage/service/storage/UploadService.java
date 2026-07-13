package moltecore.pet.cloud_storage.service.storage;

import lombok.RequiredArgsConstructor;
import moltecore.pet.cloud_storage.DTO.ResourceDTO;
import moltecore.pet.cloud_storage.exceptions.BadRequestException;
import moltecore.pet.cloud_storage.exceptions.ConflictException;
import moltecore.pet.cloud_storage.exceptions.NotFoundException;
import moltecore.pet.cloud_storage.service.MinioService;
import moltecore.pet.cloud_storage.util.StoragePathUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final MinioService minioService;

    public ResourceDTO uploadObject(int id, String path, List<MultipartFile> files) {

        if (isDirectory(files)) {
            return uploadDirectory(id, path, files);
        }

        return uploadFile(id, path, files.getFirst());
    }

    private ResourceDTO uploadFile(int id, String path, MultipartFile file) {

        String objectPath = StoragePathUtils.buildStoragePath(id, path) + file.getOriginalFilename();

        minioService.uploadFile(objectPath, file);

        return new ResourceDTO(
                StoragePathUtils.normalizeDirectoryPath(path),
                file.getOriginalFilename(),
                file.getSize(),
                "FILE"
        );
    }

    private ResourceDTO uploadDirectory(int id, String path, List<MultipartFile> files) {

        String storagePath = StoragePathUtils.buildStoragePath(id, path);

        String folderName = files.getFirst().getOriginalFilename().split("/")[0];

        minioService.createDirectory(storagePath + folderName + "/");

        for (MultipartFile file : files) {

            String fileName = file.getOriginalFilename();
            minioService.uploadFile(storagePath + fileName, file);
        }


        return new ResourceDTO(
                StoragePathUtils.normalizeDirectoryPath(path),
                folderName,
                null,
                "DIRECTORY"
        );
    }

    private boolean isDirectory(List<MultipartFile> files) {

        MultipartFile file = files.getFirst();

        return file.getOriginalFilename().contains("/");
    }

    public ResourceDTO createDirectory(int id, String path) {

        if (!path.endsWith("/")) {
            throw new BadRequestException("Путь папки должен заканчиваться на /");
        }

        String objectPath = StoragePathUtils.buildStoragePath(id, path);
        String parentPath = StoragePathUtils.getParent(objectPath);

        if (!minioService.isDirectoryExist(parentPath)) {
            throw new NotFoundException("Родительская папка не существует");
        }

        if (minioService.isDirectoryExist(objectPath)) {
            throw new ConflictException("Папка уже существует");
        }

        minioService.createDirectory(objectPath);

        return new ResourceDTO(
                StoragePathUtils.getParent(path),
                StoragePathUtils.getName(path),
                null,
                "DIRECTORY");
    }
}
