package server_group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server_group.model.Model;
import server_group.model.ProcessStep;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessStepRepository extends JpaRepository<ProcessStep, Long> {
    Optional<ProcessStep> findById(Long id);
    List<ProcessStep> findByModelName(String modelName);
    List<ProcessStep> findByModelNameOrderByStepOrder(String modelName);
    void deleteById(Long id);
    List<ProcessStep> findByModelAndStepOrderGreaterThanEqualOrderByStepOrderDesc(Model model, int order);

}
