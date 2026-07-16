package moltecore.pet.cloud_storage.service;


import lombok.RequiredArgsConstructor;
import moltecore.pet.cloud_storage.DTO.DownloadDTO;
import moltecore.pet.cloud_storage.DTO.ResourceDTO;
import moltecore.pet.cloud_storage.service.storage.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final DeleteService deleteService;
    private final DownloadService downloadService;
    private final GetResourcesService getResourcesService;
    private final RemoveService removeService;
    private final UploadService uploadService;

    public void delete(int id, String path) {
        deleteService.removeObject(id, path);
    }

    public DownloadDTO download(int id, String encodedPath) {
        String path = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);
        return downloadService.download(id, path);
    }

    public ResourceDTO getResource(int id, String path) {
        return getResourcesService.getResourceInfo(id, path);
    }

    public List<ResourceDTO> getContent(int id, String path) {
        return getResourcesService.getDirectoryContent(id, path);
    }

    public List<ResourceDTO> search(int id, String path) {
        return getResourcesService.searchObjects(id, path);
    }

    public ResourceDTO moveResource(int id, String from,  String to) {
        return removeService.moveResource(id, from, to);
    }

    public List<ResourceDTO> upload(int id, String path, List<MultipartFile> files) {
        return uploadService.upload(id, path, files);
    }

    public ResourceDTO createFolder(int id, String path) {
        return uploadService.createDirectory(id, path);
    }

}




