package moltecore.pet.cloud_storage.service.storage;

import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import moltecore.pet.cloud_storage.DTO.ResourceDTO;
import moltecore.pet.cloud_storage.exceptions.ConflictException;
import moltecore.pet.cloud_storage.exceptions.NotFoundException;
import moltecore.pet.cloud_storage.service.MinioService;
import moltecore.pet.cloud_storage.util.StoragePathUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RemoveService {

    private final MinioService minioService;

    public ResourceDTO moveResource(int id, String from, String to) {

        String oldPath = StoragePathUtils.buildStoragePath(id, from);
        String newPath = StoragePathUtils.buildStoragePath(id, to);


        if (from.endsWith("/")) {
            if (!minioService.isDirectoryExist(oldPath)) {
                throw new NotFoundException("Папка не найдена");
            }

        } else {
            if (!minioService.isObjectExist(oldPath)) {
                throw new NotFoundException("Файл не найден");
            }
        }

        if (to.endsWith("/")) {

            if (minioService.isDirectoryExist(newPath)) {
                throw new ConflictException("Ресурс уже существует");
            }

        } else {
            if (minioService.isObjectExist(newPath)) {
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

            minioService.copyObject(oldPath, newPath);
            minioService.deleteFile(oldPath);

            StatObjectResponse stat =
                    minioService.getObjectInfo(newPath);

            return new ResourceDTO(
                    StoragePathUtils.getParent(to),
                    StoragePathUtils.getName(to),
                    stat.size(),
                    "FILE"
            );
        }
    }

    private void moveDirectory(String oldPath, String newPath) {

        Iterable<Result<Item>> objects = minioService.getObjects(oldPath);

        try {
            for (Result<Item> result : objects) {

                Item item = result.get();

                String oldObject = item.objectName();
                String relativePath = oldObject.substring(oldPath.length());
                String newObject = newPath + relativePath;

                System.out.println(oldPath);
                System.out.println(newPath);

                minioService.copyObject(oldObject, newObject);
            }

            Iterable<Result<Item>> oldObjects =
                    minioService.getObjects(oldPath);

            for (Result<Item> result : oldObjects) {

                Item item = result.get();
                minioService.deleteFile(item.objectName());
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
