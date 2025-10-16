package server_group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server_group.model.Machinery;

import java.util.Optional;

@Repository
public interface MachineryRepository extends JpaRepository<Machinery, Long> {
    Optional<Machinery> findByName(String name);
}
