package server_group.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server_group.dto.ModelWithStepsDTO;
import server_group.model.Machinery;
import server_group.model.Model;
import server_group.model.ProcessStep;
import server_group.model.Raw;
import server_group.repository.ModelRepository;
import server_group.repository.ProcessStepRepository;
import server_group.repository.RawRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ModelService {
    private final ModelRepository modelRepository;
    private final ProcessStepRepository processStepRepository;
    private final RawRepository rawRepository; // Necessario per associare il Raw tramite ID

    public ModelService(ModelRepository modelRepository, ProcessStepRepository processStepRepository, RawRepository rawRepository) {
        this.modelRepository = modelRepository;
        this.processStepRepository = processStepRepository;
        this.rawRepository = rawRepository;
    }

    public List<Model> getAllModels() {
        return modelRepository.findAll();
    }

    public List<ModelWithStepsDTO> getAllModelsWithProcess() {
        List<Model> models = modelRepository.findAll();
        List<ModelWithStepsDTO> dtoList = new ArrayList<>();

        for (Model model : models) {
            ModelWithStepsDTO dto = new ModelWithStepsDTO();
            dto.setName(model.getName());
            dto.setPrice(model.getPrice());

            // Raw
            Raw raw = model.getRaw();
            ModelWithStepsDTO.RawDTO rawDTO = new ModelWithStepsDTO.RawDTO();
            rawDTO.setMaterial(raw.getMaterial());
            rawDTO.setShape(raw.getShape());
            rawDTO.setSize(raw.getSize());
            dto.setRaw(rawDTO);

            // Steps
            List<ProcessStep> steps = processStepRepository.findByModelNameOrderByStepOrder(model.getName());
            List<ModelWithStepsDTO.ProcessStepDTO> stepDTOs = new ArrayList<>();

            for (ProcessStep step : steps) {
                ModelWithStepsDTO.ProcessStepDTO stepDTO = new ModelWithStepsDTO.ProcessStepDTO();
                stepDTO.setId(step.getId());
                stepDTO.setStepOrder(step.getStepOrder());
                stepDTO.setDuration(step.getDuration());
                stepDTO.setSemifinishedName(step.getSemifinishedName());

                Machinery machinery = step.getMachinery();
                ModelWithStepsDTO.MachineryDTO machineryDTO = new ModelWithStepsDTO.MachineryDTO();
                machineryDTO.setName(machinery.getName());
                stepDTO.setMachinery(machineryDTO);

                stepDTOs.add(stepDTO);
            }

            dto.setProcessSteps(stepDTOs);
            dtoList.add(dto);
        }

        return dtoList;
    }

    // GET
    public Optional<Model> getModelByName(String name) {
        return modelRepository.findById(name);
    }

    // CREATE
    public Model saveModel(Model model) {
        if (model.getRaw() != null && model.getRaw().getId() != null) {
            Raw raw = rawRepository.findById(model.getRaw().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Raw not found with ID: " + model.getRaw().getId()));
            model.setRaw(raw);
        }
        return modelRepository.save(model);
    }

    // UPDATE
    public Optional<Model> updateModel(String name, Model updatedModel) {
        return modelRepository.findById(name).map(existing -> {
            existing.setPrice(updatedModel.getPrice());

            if (updatedModel.getRaw() != null && updatedModel.getRaw().getId() != null) {
                Raw raw = rawRepository.findById(updatedModel.getRaw().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Raw not found with ID: " + updatedModel.getRaw().getId()));
                existing.setRaw(raw);
            }

            return modelRepository.save(existing);
        });
    }

    // DELETE
    public boolean deleteModel(String name) {
        if (modelRepository.existsById(name)) {
            modelRepository.deleteById(name);
            return true;
        }
        return false;
    }
}
