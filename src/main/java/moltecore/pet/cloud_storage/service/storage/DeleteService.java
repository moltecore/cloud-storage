package moltecore.pet.cloud_storage.service.storage;

import lombok.RequiredArgsConstructor;
import moltecore.pet.cloud_storage.exceptions.NotFoundException;
import moltecore.pet.cloud_storage.service.MinioService;
import moltecore.pet.cloud_storage.util.StoragePathUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteService {

    private final MinioService minioService;

    public void removeObject(int id, String path) {

        String objectPath = StoragePathUtils.buildStoragePath(id, path);



        if (path.endsWith("/")) {

            if (!minioService.isDirectoryExist(objectPath)) {
                throw new NotFoundException("Папка не найдена");
            }

            minioService.deleteDirectory(objectPath);

            return;
        }


        if (!minioService.isObjectExist(objectPath)) {
            throw new NotFoundException("Файл не найден");
        }

        minioService.deleteFile(objectPath);
    }

}
