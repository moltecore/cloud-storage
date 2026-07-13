package moltecore.pet.cloud_storage.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserDTO {

    @NotBlank(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 100, message = "Имя от 2 до 100 символов")
    private String username;

    @NotBlank(message = "Пароль не введен")
    @Size(min = 5, max = 100, message = "Пароль от 5 до 100 символов")
    private  String password;

}
