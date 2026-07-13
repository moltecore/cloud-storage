package moltecore.pet.cloud_storage.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceDTO(
        String path,
        String name,
        Long size,
        String type
) {

}