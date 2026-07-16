package moltecore.pet.cloud_storage.service.storage;

import lombok.RequiredArgsConstructor;
import moltecore.pet.cloud_storage.exceptions.NotFoundException;
import moltecore.pet.cloud_storage.service.interfaces.StorageService;
import moltecore.pet.cloud_storage.util.StoragePathUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteService {

    private final StorageService storageService;

    public void removeObject(int id, String path) {

        String objectPath = StoragePathUtils.buildStoragePath(id, path);

        if (path.endsWith("/")) {

            if (!storageService.isDirectoryExist(objectPath)) {
                throw new NotFoundException("Папка не найдена");
            }

            storageService.deleteDirectory(objectPath);

            return;
        }


        if (!storageService.isObjectExist(objectPath)) {
            throw new NotFoundException("Файл не найден");
        }

        storageService.deleteFile(objectPath);
    }

}
