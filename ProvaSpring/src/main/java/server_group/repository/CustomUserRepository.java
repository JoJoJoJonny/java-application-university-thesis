package server_group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server_group.model.CustomUser;
import server_group.model.CustomUserDetails;
import server_group.security.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomUserRepository extends JpaRepository<CustomUser, String> {
    Optional<CustomUser> findByEmail(String email);
    boolean existsByEmail(String email);

    List<CustomUser> findByRole(Role role);

}
