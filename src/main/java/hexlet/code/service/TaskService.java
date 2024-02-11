package hexlet.code.service;

import hexlet.code.dto.TaskDTO.TaskCreateDTO;
import hexlet.code.dto.TaskDTO.TaskDTO;
import hexlet.code.dto.TaskDTO.TaskFilterDTO;
import hexlet.code.dto.TaskDTO.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.specification.TaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static hexlet.code.util.ListUtils.getPageRequest;

@Service
public class TaskService {

    private static final String NOT_FOUND_MESSAGE = "Task not found";

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskSpecification taskSpecification;

    public Long countAll(TaskFilterDTO taskFilterDTO) {
        var taskSpec = taskSpecification.build(taskFilterDTO);
        return taskRepository.count(taskSpec);
    }

    public List<TaskDTO> getAll(
            TaskFilterDTO taskFilterDTO,
            Integer start,
            Integer end,
            String orderDirection,
            String orderProperty
    ) {
        var pageRequest = getPageRequest(start, end, orderDirection, orderProperty);
        var taskSpec = taskSpecification.build(taskFilterDTO);

        return taskRepository.findAll(taskSpec, pageRequest)
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO findById(Long id) {
        var taskStatus = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));

        return taskMapper.map(taskStatus);
    }

    public TaskDTO create(TaskCreateDTO data) {
        var task = taskMapper.map(data);
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public TaskDTO update(Long id, TaskUpdateDTO data) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));
        taskMapper.update(data, task);
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
