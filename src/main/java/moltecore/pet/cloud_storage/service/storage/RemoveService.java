package moltecore.pet.cloud_storage.service.storage;

import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import moltecore.pet.cloud_storage.DTO.ResourceDTO;
import moltecore.pet.cloud_storage.exceptions.ConflictException;
import moltecore.pet.cloud_storage.exceptions.NotFoundException;
import moltecore.pet.cloud_storage.service.interfaces.StorageService;
import moltecore.pet.cloud_storage.util.StoragePathUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RemoveService {

    private final StorageService storageService;

    public ResourceDTO moveResource(int id, String from, String to) {

        String oldPath = StoragePathUtils.buildStoragePath(id, from);
        String newPath = StoragePathUtils.buildStoragePath(id, to);


        if (from.endsWith("/")) {
            if (!storageService.isDirectoryExist(oldPath)) {
                throw new NotFoundException("Папка не найдена");
            }

        } else {
            if (!storageService.isObjectExist(oldPath)) {
                throw new NotFoundException("Файл не найден");
            }
        }

        if (to.endsWith("/")) {

            if (storageService.isDirectoryExist(newPath)) {
                throw new ConflictException("Ресурс уже существует");
            }

        } else {
            if (storageService.isObjectExist(newPath)) {
                throw new ConflictException("Ресурс уже существует");
            }
        }

        if (from.endsWith("/")) {

            moveDirectory(oldPath, newPath);

            return new ResourceDTO(
                    StoragePathUtils.getParent(to),
                    StoragePathUtils.getName(to),
                    null,
                    "DIRECTORY"
            );

        }

        else {

            storageService.copyObject(oldPath, newPath);
            storageService.deleteFile(oldPath);

            StatObjectResponse stat =
                    storageService.getObjectInfo(newPath);

            return new ResourceDTO(
                    StoragePathUtils.getParent(to),
                    StoragePathUtils.getName(to),
                    stat.size(),
                    "FILE"
            );
        }
    }

    private void moveDirectory(String oldPath, String newPath) {

        Iterable<Result<Item>> objects = storageService.getObjects(oldPath);

        try {
            for (Result<Item> result : objects) {

                Item item = result.get();

                String oldObject = item.objectName();
                String relativePath = oldObject.substring(oldPath.length());
                String newObject = newPath + relativePath;

                System.out.println(oldPath);
                System.out.println(newPath);

                storageService.copyObject(oldObject, newObject);
            }

            Iterable<Result<Item>> oldObjects =
                    storageService.getObjects(oldPath);

            for (Result<Item> result : oldObjects) {

                Item item = result.get();
                storageService.deleteFile(item.objectName());
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
