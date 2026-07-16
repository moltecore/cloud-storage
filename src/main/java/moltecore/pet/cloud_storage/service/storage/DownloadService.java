package moltecore.pet.cloud_storage.service.storage;

import io.minio.GetObjectResponse;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import moltecore.pet.cloud_storage.DTO.DownloadDTO;
import moltecore.pet.cloud_storage.exceptions.NotFoundException;
import moltecore.pet.cloud_storage.service.interfaces.StorageService;
import moltecore.pet.cloud_storage.util.StoragePathUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class DownloadService {

    private final StorageService storageService;

    private DownloadDTO downloadFile(int id, String path) {

        String objectPath = StoragePathUtils.buildStoragePath(id, path);

        if (!storageService.isObjectExist(objectPath)) {
            throw new NotFoundException("Такого файла не существует");
        }

        GetObjectResponse file = storageService.downloadFile(objectPath);

        StreamingResponseBody body = outputStream -> {
            try (file) {
                file.transferTo(outputStream);
            }
        };

        String fileName = path.substring(path.lastIndexOf("/") + 1);
        return new DownloadDTO(body, fileName);
    }

    private DownloadDTO downloadDirectory(int id, String path) {

        String objectPath = StoragePathUtils.buildStoragePath(id, path);

        if (!storageService.isDirectoryExist(objectPath)) {
            throw new NotFoundException("Такой папки не существует");
        }

        StreamingResponseBody body = (outputStream) -> {

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

                Iterable<Result<Item>> results = storageService.getObjects(objectPath);


                for (Result<Item> result : results) {

                    Item item = result.get();
                    String objectName = item.objectName();
                    String zipPath = objectName.substring(objectPath.length());

                    if (zipPath.isBlank()) {
                        continue;
                    }

                    ZipEntry entry = new ZipEntry(zipPath);
                    zipOutputStream.putNextEntry(entry);

                    if (!objectName.endsWith("/")) {

                        try (GetObjectResponse file = storageService.downloadFile(objectName)) {

                            file.transferTo(zipOutputStream);
                        }
                    }

                    zipOutputStream.closeEntry();
                }

                zipOutputStream.finish();

            } catch (Exception e) {
                throw new RuntimeException("Ошибка создания архива", e);
            }
        };



        return new DownloadDTO(
                body,
                "archive.zip"
        );
    }

    public DownloadDTO download(int id, String path) {

        if (path.endsWith("/")) {
            return downloadDirectory(id, path);
        }

        return downloadFile(id, path);
    }
}
