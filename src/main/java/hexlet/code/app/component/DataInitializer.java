package hexlet.code.app.component;

import hexlet.code.app.dto.TaskStatusCreateDTO;
import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final UserMapper userMapper;

    @Autowired
    private final TaskStatusMapper taskStatusMapper;

    @Autowired
    private final TaskStatusRepository taskStatusRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.findByEmail("hexlet@example.com").isEmpty()) {
            var userData = new UserCreateDTO();
            userData.setEmail("hexlet@example.com");
            userData.setPassword("qwerty");
            var user = userMapper.map(userData);
            userRepository.save(user);
        }


        if (taskStatusRepository.count() == 0) {
            var taskStatuses = Map.of(
                            "Draft", "draft",
                            "ToReview", "to_review",
                            "ToBeFixed", "to_be_fixed",
                            "ToPublish", "to_publish",
                            "Published", "published"
                    ).entrySet()
                    .stream()
                    .map(entry -> {
                        var taskStatusData = new TaskStatusCreateDTO();
                        taskStatusData.setName(entry.getKey());
                        taskStatusData.setSlug(entry.getValue());
                        return taskStatusMapper.map(taskStatusData);
                    });
            taskStatuses.forEach(taskStatusRepository::save);
        }
    }
}
