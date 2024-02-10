package hexlet.code.app.service;

import hexlet.code.app.dto.LabelDTO.LabelCreateDTO;
import hexlet.code.app.dto.LabelDTO.LabelDTO;
import hexlet.code.app.dto.LabelDTO.LabelUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static hexlet.code.app.util.ListUtils.getPageRequest;

@Service
public class LabelService {

    private static final String NOT_FOUND_MESSAGE = "Label not found";

    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private LabelRepository labelRepository;


    public Long countAll() {
        return labelRepository.count();
    }

    public List<LabelDTO> getAll(Integer start, Integer end, String orderDirection, String orderProperty) {
        var pageRequest = getPageRequest(start, end, orderDirection, orderProperty);

        return labelRepository.findAll(pageRequest)
                .stream()
                .map(labelMapper::map)
                .toList();
    }


    public LabelDTO findById(Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));

        return labelMapper.map(label);
    }

    public LabelDTO create(LabelCreateDTO data) {
        var label = labelMapper.map(data);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public LabelDTO update(Long id, LabelUpdateDTO data) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));
        labelMapper.update(data, label);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public void delete(Long id) {
        labelRepository.deleteById(id);
    }
}
