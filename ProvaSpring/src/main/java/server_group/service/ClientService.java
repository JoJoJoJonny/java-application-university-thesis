package server_group.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server_group.model.Client;
import server_group.repository.ClientRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Optional<Client> findByPiva(String piva) {
        return clientRepository.findById(piva);
    }

    public Client save(Client client) {
        return clientRepository.save(client);
    }

    public void deleteByPiva(String piva) {
        clientRepository.deleteById(piva);
    }
}
