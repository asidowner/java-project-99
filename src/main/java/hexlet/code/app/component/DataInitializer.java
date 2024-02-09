package hexlet.code.app.component;

import hexlet.code.app.dto.TaskStatusDTO.TaskStatusCreateDTO;
import hexlet.code.app.dto.UserDTO.UserCreateDTO;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
class DataInitializer implements ApplicationRunner {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final UserMapper userMapper;

    @Autowired
    private final TaskStatusMapper taskStatusMapper;

    @Autowired
    private final TaskStatusRepository taskStatusRepository;

    @Autowired
    private final AdminProperties adminProperties;

    @Autowired
    private final TaskStatusProperties taskStatusProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.findByEmail("hexlet@example.com").isEmpty()) {
            var userData = new UserCreateDTO();
            userData.setEmail(adminProperties.getEmail());
            userData.setPassword(adminProperties.getPassword());
            var user = userMapper.map(userData);
            userRepository.save(user);
        }


        if (taskStatusRepository.count() == 0) {
            var taskStatuses = taskStatusProperties.getDefaultStatuses()
                    .stream()
                    .map(map -> {
                        var taskStatusData = new TaskStatusCreateDTO();
                        taskStatusData.setName(map.get("name"));
                        taskStatusData.setSlug(map.get("slug"));
                        return taskStatusMapper.map(taskStatusData);
                    });
            taskStatuses.forEach(taskStatusRepository::save);
        }
    }
}
