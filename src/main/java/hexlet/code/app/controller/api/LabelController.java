package hexlet.code.app.controller.api;

import hexlet.code.app.dto.LabelDTO.LabelCreateDTO;
import hexlet.code.app.dto.LabelDTO.LabelDTO;
import hexlet.code.app.dto.LabelDTO.LabelUpdateDTO;
import hexlet.code.app.service.LabelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/labels")
public class LabelController {

    @Autowired
    private LabelService labelService;

    @GetMapping
    public ResponseEntity<List<LabelDTO>> index(
            @RequestParam(defaultValue = "0", name = "_start") Integer start,
            @RequestParam(defaultValue = "100", name = "_end") Integer end,
            @RequestParam(defaultValue = "ASC", name = "_order") String orderDirection,
            @RequestParam(defaultValue = "id", name = "_sort") String orderProperty
    ) {
        var labels = labelService.getAll(start, end, orderDirection, orderProperty);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labelService.countAll()))
                .body(labels);
    }


    @GetMapping("/{id}")
    public LabelDTO show(@PathVariable Long id) {
        return labelService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO create(@Valid @RequestBody LabelCreateDTO data) {
        return labelService.create(data);
    }

    @PutMapping("/{id}")
    public LabelDTO update(@PathVariable Long id, @Valid @RequestBody LabelUpdateDTO data) {
        return labelService.update(id, data);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        labelService.delete(id);
    }

}
