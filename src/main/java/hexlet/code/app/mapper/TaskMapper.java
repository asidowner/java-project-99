package hexlet.code.app.mapper;

import hexlet.code.app.dto.TaskDTO.TaskCreateDTO;
import hexlet.code.app.dto.TaskDTO.TaskDTO;
import hexlet.code.app.dto.TaskDTO.TaskUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.model.Task;
import hexlet.code.app.repository.TaskStatusRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    private static final String STATUS_NOT_FOUND_MESSAGE = "Status not found";

    @Mapping(target = "assignee.id", source = "assigneeId")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "status", source = "taskStatus.slug")
    public abstract TaskDTO map(Task model);

    @Mapping(target = "assignee.id", source = "assigneeId")
    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);

    @AfterMapping
    public void setStatus(TaskUpdateDTO dto, @MappingTarget Task model) {
        if (dto.getStatus().isPresent()) {
            var status = taskStatusRepository.findBySlug(dto.getStatus().get())
                    .orElseThrow(() -> new ResourceNotFoundException(STATUS_NOT_FOUND_MESSAGE));
            model.setTaskStatus(status);
        }
    }

    @AfterMapping
    public void setStatus(TaskCreateDTO dto, @MappingTarget Task model) {
        var status = taskStatusRepository.findBySlug(dto.getStatus())
                .orElseThrow(() -> new ResourceNotFoundException(STATUS_NOT_FOUND_MESSAGE));
        model.setTaskStatus(status);
    }
}
