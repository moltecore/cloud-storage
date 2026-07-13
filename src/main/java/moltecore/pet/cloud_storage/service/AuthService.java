package moltecore.pet.cloud_storage.service;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import moltecore.pet.cloud_storage.DTO.UserDTO;
import moltecore.pet.cloud_storage.exceptions.ConflictException;
import moltecore.pet.cloud_storage.mapper.UserMapper;
import moltecore.pet.cloud_storage.models.User;
import moltecore.pet.cloud_storage.repositories.UsersRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final UsersRepositories userRepositorie;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MinioService minioService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UsersRepositories userRepositorie, UserMapper userMapper, PasswordEncoder passwordEncoder, MinioService minioService,  AuthenticationManager authenticationManager) {
        this.userRepositorie = userRepositorie;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.minioService = minioService;
        this.authenticationManager = authenticationManager;
    }


    public void authenticate(UserDTO userDTO) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userDTO.getUsername(),
                        userDTO.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public void register(UserDTO userDTO) {
        saveUser(userDTO);
        authenticate(userDTO);
    }

    public void login(UserDTO userDTO) {
        authenticate(userDTO);
    }

    public void logout(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }


    public void saveUser(UserDTO userDTO) {

        User user = userMapper.toUser(userDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            userRepositorie.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Пользователь уже существует");
        }
        minioService.createDirectory("user-" + user.getId() + "-files/");
    }



}
