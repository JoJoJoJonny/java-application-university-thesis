package server_group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server_group.model.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {}