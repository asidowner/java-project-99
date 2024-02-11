package hexlet.code.app.dto.TaskStatusDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusCreateDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String slug;
}