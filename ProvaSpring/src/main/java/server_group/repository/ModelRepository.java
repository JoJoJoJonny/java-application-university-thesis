package server_group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server_group.model.Machinery;
import server_group.model.Model;

import java.util.Optional;

@Repository
public interface ModelRepository extends JpaRepository<Model, String> {
    Optional<Model> findByName(String name);
}
