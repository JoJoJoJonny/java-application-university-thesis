package server_group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import server_group.dto.ModelWithStepsDTO;
import server_group.model.Model;
import server_group.service.ModelService;


import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    //tutti i modelli
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE', 'ACCOUNTANT')")
    @GetMapping
    public List<Model> getAll() {
        return modelService.getAllModels();
    }

    //modelli con processi
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE', 'ACCOUNTANT')")
    @GetMapping("/processes")
    public ResponseEntity<List<ModelWithStepsDTO>> getModelsWithProcesses() {
        List<ModelWithStepsDTO> result = modelService.getAllModelsWithProcess();
        return ResponseEntity.ok(result);
    }

    //modello per nome
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE', 'ACCOUNTANT')")
    @GetMapping("/{name}")
    public ResponseEntity<Model> getByName(@PathVariable String name) {
        Optional<Model> model = modelService.getModelByName(name);
        return model.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //crea un nuovo modello
    @PreAuthorize("hasAnyRole('MANAGER')")
    @PostMapping
    public ResponseEntity<Model> create(@RequestBody Model model) {
        Model created = modelService.saveModel(model);
        return ResponseEntity.created(URI.create("/api/models/" + created.getName())).body(created);
    }

    //aggiorna un modello esistente
    @PreAuthorize("hasAnyRole('MANAGER')")
    @PutMapping("/{name}")
    public ResponseEntity<Model> update(@PathVariable String name, @RequestBody Model updatedModel) {
        Optional<Model> updated = modelService.updateModel(name, updatedModel);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //elimina un modello
    @PreAuthorize("hasAnyRole('MANAGER')")
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> delete(@PathVariable String name) {
        boolean deleted = modelService.deleteModel(name);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
