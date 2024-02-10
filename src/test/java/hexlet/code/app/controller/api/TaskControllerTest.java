package hexlet.code.app.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelGenerator;
import hexlet.code.app.util.UserUtils;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Set;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {
    private Task testTask;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Faker faker;

    @BeforeEach
    public void setUp() {
        TaskStatus testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(testTaskStatus);

        Label testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(testLabel);

        testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        testTask.setTaskStatus(testTaskStatus);
        testTask.setAssignee(userUtils.getTestUser());
        testTask.setLabels(Set.of(testLabel));

        token = jwt().jwt(builder -> builder.subject(userUtils.getTestUser().getEmail()));
    }


    @Test
    public void testIndex() throws Exception {
        taskRepository.save(testTask);
        var result = mockMvc.perform(get("/api/tasks").with(token))
                .andExpect(status().isOk())
                .andReturn();

        var totalCount = result.getResponse().getHeader("X-Total-Count");

        assertThat(totalCount).isNotNull();
        assertThat(Long.valueOf(totalCount)).isEqualTo(taskRepository.count());

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testIndexOrderAndPaging() throws Exception {
        var start = 10;
        var end = 20;
        var order = "DESC";
        var sort = "id";

        var request = get("/api/tasks")
                .with(token)
                .param("_start", String.valueOf(start))
                .param("_end", String.valueOf(end))
                .param("_order", order)
                .param("_sort", sort);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().isEmpty();
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {
        taskRepository.save(testTask);
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testShow() throws Exception {
        taskRepository.save(testTask);
        var request = get("/api/tasks/{id}", testTask.getId()).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("index").isEqualTo(testTask.getIndex()),
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("content").isEqualTo(testTask.getDescription()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug()),
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId()),
                v -> v.node("createdAt").isPresent()
        );
    }

    @Test
    public void testShowNegative() throws Exception {
        var request = get("/api/tasks/{id}", 99999).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testShowWithoutAuth() throws Exception {
        taskRepository.save(testTask);
        var request = get("/api/tasks/{id}", testTask.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreate() throws Exception {
        var taskCreateRequest = getTaskCreateRequest();

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(taskCreateRequest));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var task = taskRepository.findByName(testTask.getName());

        assertThat(task.isPresent()).isTrue();
        assertThat(task.get().getDescription()).isEqualTo(testTask.getDescription());
        assertThat(task.get().getAssignee()).isEqualTo(userUtils.getTestUser());
        assertThat(task.get().getTaskStatus()).isEqualTo(testTask.getTaskStatus());
        assertThat(task.get().getCreatedAt()).isNotNull();
    }

    @Test
    public void testCreateNegative() throws Exception {
        var taskCreateRequest = Map.of(
                "index", testTask.getIndex(),
                "title", "",
                "content", testTask.getDescription()
        );

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(taskCreateRequest));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        var taskCreateRequest = getTaskCreateRequest();

        var request = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(taskCreateRequest));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }


    private Map<String, ?> getTaskCreateRequest() {
        return Map.of(
                "index", testTask.getIndex(),
                "title", testTask.getName(),
                "content", testTask.getDescription(),
                "assignee_id", testTask.getAssignee().getId(),
                "status", testTask.getTaskStatus().getSlug(),
                "taskLabelIds", testTask.getLabels()
                        .stream()
                        .map(Label::getId)
                        .toList()
        );
    }


    @Test
    public void testUpdate() throws Exception {
        taskRepository.save(testTask);

        var newStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(newStatus);

        var newUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser);

        var updateTask = Map.of(
                "title", faker.text().text(),
                "index", faker.number().randomDigit(),
                "status", newStatus.getSlug(),
                "assignee_id", newUser.getId()
        );

        var request = put("/api/tasks/{id}", testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateTask));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var task = taskRepository.findById(testTask.getId());
        assertThat(task.isPresent()).isTrue();
        assertThat(task.get().getName()).isEqualTo(updateTask.get("title"));
        assertThat(task.get().getIndex()).isEqualTo(updateTask.get("index"));
        assertThat(task.get().getTaskStatus()).isEqualTo(newStatus);
        assertThat(task.get().getAssignee()).isEqualTo(newUser);
    }

    @Test
    public void testUpdateNegative() throws Exception {
        var updateTask = Map.of(
                "title", faker.text().text(),
                "index", faker.number().randomDigit()
        );
        var request = put("/api/tasks/{id}", 999999)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateTask));

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateWithoutAuth() throws Exception {
        taskRepository.save(testTask);
        var updateTask = Map.of(
                "title", faker.text().text(),
                "index", faker.number().randomDigit()
        );
        var request = put("/api/tasks/{id}", testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateTask));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }


    @Test
    public void testDelete() throws Exception {
        taskRepository.save(testTask);

        var request = delete("/api/tasks/{id}", testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var task = taskRepository.findById(testTask.getId());
        assertThat(task.isEmpty()).isTrue();
    }

    @Test
    public void testDeleteWithoutAuth() throws Exception {
        taskRepository.save(testTask);

        var request = delete("/api/task_statuses/{id}", testTask.getId())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
