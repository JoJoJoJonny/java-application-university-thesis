package server_group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server_group.model.Raw;

@Repository
public interface RawRepository extends JpaRepository<Raw, Long> {
}
