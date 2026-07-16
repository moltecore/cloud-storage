package moltecore.pet.cloud_storage.service.interfaces;

import io.minio.GetObjectResponse;
import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    boolean isObjectExist(String objectName);

    void uploadFile(String objectName, MultipartFile file);

    void createDirectory(String objectName);

    void deleteFile(String objectName);

    Iterable<Result<Item>> getObjects(String prefix);

    GetObjectResponse downloadFile(String objectName);

    void deleteDirectory(String prefix);

    Iterable<Result<Item>> getDirectory(String prefix);

    StatObjectResponse getObjectInfo(String objectName);

    boolean isDirectoryExist(String path);

    void copyObject(String sourceObject, String destinationObject);

    boolean isResourceExist(String objectName);

}
