package moltecore.pet.cloud_storage.mapper;


import moltecore.pet.cloud_storage.DTO.UserDTO;

import moltecore.pet.cloud_storage.models.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserDTO userDTO);
}
