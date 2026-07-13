package moltecore.pet.cloud_storage.service.storage;

import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import moltecore.pet.cloud_storage.DTO.ResourceDTO;
import moltecore.pet.cloud_storage.exceptions.BadRequestException;
import moltecore.pet.cloud_storage.exceptions.NotFoundException;
import moltecore.pet.cloud_storage.service.MinioService;
import moltecore.pet.cloud_storage.util.StoragePathUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetResourcesService {

    private final MinioService minioService;

    public ResourceDTO getResourceInfo(int id, String path) {

        String objectPath = StoragePathUtils.buildStoragePath(id, path);

        if (path.endsWith("/")) {

            if (!minioService.isDirectoryExist(objectPath)) {
                throw new NotFoundException("Папка не существует");
            }

            return new ResourceDTO(
                    StoragePathUtils.getParent(path),
                    StoragePathUtils.getName(path),
                    null,
                    "DIRECTORY"
            );

        } else {

            if (!minioService.isObjectExist(objectPath)) {
                throw new NotFoundException("Файл не существует");
            }

            StatObjectResponse stat = minioService.getObjectInfo(objectPath);

            return new ResourceDTO(
                    StoragePathUtils.getParent(path),
                    StoragePathUtils.getName(path),
                    stat.size(),
                    "FILE"
            );
        }
    }

    public List<ResourceDTO> getDirectoryContent(int id, String path) {

        String objectPath = StoragePathUtils.buildStoragePath(id, path);

        if (!minioService.isDirectoryExist(objectPath)) {
            throw new NotFoundException("Папка не найдена");
        }

        List<ResourceDTO> resources = new ArrayList<>();

        String userPrefix = "user-" + id + "-files/";

        try {

            Iterable<Result<Item>> results =
                    minioService.getDirectory(objectPath);


            for (Result<Item> result : results) {

                Item item = result.get();
                if (item.objectName().equals(objectPath)) {
                    continue;
                }

                String clientPath = item.objectName().substring(userPrefix.length());


                if (clientPath.endsWith("/")) {

                    resources.add(new ResourceDTO(
                                    StoragePathUtils.getParent(clientPath),
                                    StoragePathUtils.getName(clientPath),
                                    null,
                                    "DIRECTORY"
                            )
                    );

                } else {
                    resources.add(new ResourceDTO(
                                    StoragePathUtils.getParent(clientPath),
                                    StoragePathUtils.getName(clientPath),
                                    item.size(),
                                    "FILE"
                            )
                    );
                }
            }

            return resources;


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<ResourceDTO> searchObjects(int id, String query) {

        if (query == null || query.isBlank()) {
            throw new BadRequestException("Поисковый запрос пустой");
        }

        List<ResourceDTO> resources = new ArrayList<>();

        String prefix = "user-" + id + "-files/";

        Iterable<Result<Item>> results = minioService.getObjects(prefix);

        try {

            for (Result<Item> result : results) {

                Item item = result.get();
                String objectName = item.objectName();
                String clientPath = objectName.substring(prefix.length());
                boolean isDirectory = objectName.endsWith("/");
                String name = StoragePathUtils.getName(clientPath);

                if (!name.toLowerCase().contains(query.toLowerCase())) {
                    continue;
                }

                resources.add(new ResourceDTO(
                                StoragePathUtils.getParent(clientPath),
                                name,
                                isDirectory ? null : item.size(),
                                isDirectory ? "DIRECTORY" : "FILE"
                        )
                );
            }

            return resources;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
