package server_group.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server_group.model.Raw;
import server_group.repository.RawRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RawService {

    private final RawRepository rawRepository;

    public RawService(RawRepository rawRepository) {
        this.rawRepository = rawRepository;
    }

    public List<Raw> findAll() {
        return rawRepository.findAll();
    }

    public Optional<Raw> findById(Long id) {
        return rawRepository.findById(id);
    }

    public Raw save(Raw raw) {
        return rawRepository.save(raw);
    }

    public void delete(Long id) {
        rawRepository.deleteById(id);
    }
}

