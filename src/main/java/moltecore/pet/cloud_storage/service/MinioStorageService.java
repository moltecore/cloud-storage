package moltecore.pet.cloud_storage.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import moltecore.pet.cloud_storage.service.interfaces.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;

    @Autowired
    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public boolean isObjectExist(String objectName) {

        StatObjectArgs args = StatObjectArgs.builder()
                .bucket("user-files")
                .object(objectName)
                .build();

        try {
            minioClient.statObject(args);
            return true;

        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }

            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void uploadFile(String objectName, MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();

            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket("user-files")
                    .object(objectName)
                    .stream(inputStream, file.getSize(), (long) -1)
                    .build();

            minioClient.putObject(args);

        } catch (IOException | MinioException e) {
            throw new RuntimeException(e);
        }
    }

    public void createDirectory(String objectName) {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        PutObjectArgs args = PutObjectArgs.builder()
                .bucket("user-files")
                .object(objectName)
                .stream(inputStream, 0L, (long) -1)
                .build();

        try {
            minioClient.putObject(args);
        } catch (MinioException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFile(String objectName) {
        try {

            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket("user-files")
                    .object(objectName)
                    .build();

            minioClient.removeObject(args);
        } catch (MinioException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterable<Result<Item>> getObjects(String prefix) {

        try {

            ListObjectsArgs args = ListObjectsArgs.builder()
                    .bucket("user-files")
                    .prefix(prefix)
                    .recursive(true)
                    .build();

            return minioClient.listObjects(args);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public GetObjectResponse downloadFile(String objectName) {

        try {

            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket("user-files")
                    .object(objectName)
                    .build();

            return minioClient.getObject(args);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteDirectory(String prefix) {

        try {

            Iterable<Result<Item>> results = getObjects(prefix);

            for (Result<Item> result : results) {

                Item item = result.get();

                if (!item.objectName().equals(prefix)) {
                    deleteFile(item.objectName());
                }
            }

            deleteFile(prefix);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Iterable<Result<Item>> getDirectory(String prefix) {

        try {
            ListObjectsArgs args = ListObjectsArgs.builder()
                    .bucket("user-files")
                    .prefix(prefix)
                    .recursive(false)
                    .build();

            return minioClient.listObjects(args);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public StatObjectResponse getObjectInfo(String objectName) {

        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket("user-files")
                            .object(objectName)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isDirectoryExist(String path) {


        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket("user-files")
                .prefix(path)
                .maxKeys(1)
                .build();

        try {

            Iterable<Result<Item>> results = minioClient.listObjects(args);

            for (Result<Item> result : results) {
                result.get();
                return true;
            }

            return false;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void copyObject(String sourceObject, String destinationObject) {

        try {

            SourceObject source = SourceObject.builder()
                    .bucket("user-files")
                    .object(sourceObject)
                    .build();


            CopyObjectArgs args = CopyObjectArgs.builder()
                    .bucket("user-files")
                    .object(destinationObject)
                    .source(source)
                    .build();


            minioClient.copyObject(args);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isResourceExist(String objectName) {

        if (isObjectExist(objectName)) {
            return true;
        }

        return isDirectoryExist(objectName + "/");
    }


}
