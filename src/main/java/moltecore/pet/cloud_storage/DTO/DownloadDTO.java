package moltecore.pet.cloud_storage.DTO;



import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public record DownloadDTO(
        StreamingResponseBody body,
        String fileName
) {

}
