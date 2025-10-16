package server_group.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server_group.model.Machinery;
import server_group.repository.MachineryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MachineryService {

    private final MachineryRepository machineryRepository;

    public MachineryService(MachineryRepository machineryRepository) {
        this.machineryRepository = machineryRepository;
    }

    public List<Machinery> findAll() {
        return machineryRepository.findAll();
    }

    public Optional<Machinery> findById(Long id) {
        return machineryRepository.findById(id);
    }

    public Optional<Machinery> findByName(String name) {
        return machineryRepository.findByName(name);
    }

    public Machinery save(Machinery machinery) {
        return machineryRepository.save(machinery);
    }

    public void delete(Long id) {
        machineryRepository.deleteById(id);
    }
}
