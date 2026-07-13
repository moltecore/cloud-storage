package moltecore.pet.cloud_storage.service;


import moltecore.pet.cloud_storage.models.User;
import moltecore.pet.cloud_storage.repositories.UsersRepositories;
import moltecore.pet.cloud_storage.security.UsersDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsersDetailsService implements UserDetailsService {
    private final UsersRepositories userRepositories;

    public UsersDetailsService(UsersRepositories userRepositories) {
        this.userRepositories = userRepositories;
    }




    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepositories.findByUsername(username);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("username not found");
        }


        return new UsersDetails(user.get());
    }
}
