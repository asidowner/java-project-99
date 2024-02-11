package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.util.ModelGenerator;
import hexlet.code.util.UserUtils;
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
public class TaskStatusControllerTest {
    private TaskStatus testTaskStatus;

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
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private Faker faker;

    @BeforeEach
    public void setUp() {
        testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        token = jwt().jwt(builder -> builder.subject(userUtils.getTestUser().getEmail()));
    }

    @Test
    public void testIndex() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        var result = mockMvc.perform(get("/api/task_statuses").with(token))
                .andExpect(status().isOk())
                .andReturn();

        var totalCount = result.getResponse().getHeader("X-Total-Count");

        assertThat(totalCount).isNotNull();
        assertThat(Long.valueOf(totalCount)).isEqualTo(taskStatusRepository.count());

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testIndexOrderAndPaging() throws Exception {
        var start = 10;
        var end = 20;
        var order = "DESC";
        var sort = "id";

        var request = get("/api/task_statuses")
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
        taskStatusRepository.save(testTaskStatus);
        mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testShow() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        var request = get("/api/task_statuses/{id}", testTaskStatus.getId()).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testTaskStatus.getName()),
                v -> v.node("slug").isEqualTo(testTaskStatus.getSlug()),
                v -> v.node("createdAt").isPresent()
        );
    }

    @Test
    public void testShowNegative() throws Exception {
        var request = get("/api/task_statuses/{id}", 99999).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testShowWithoutAuth() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        var request = get("/api/task_statuses/{id}", testTaskStatus.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreate() throws Exception {
        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testTaskStatus));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var taskStatus = taskStatusRepository.findBySlug(testTaskStatus.getSlug());

        assertThat(taskStatus.isPresent()).isTrue();
        assertThat(taskStatus.get().getName()).isEqualTo(testTaskStatus.getName());
        assertThat(taskStatus.get().getCreatedAt()).isNotNull();
    }

    @Test
    public void testCreateNegative() throws Exception {
        testTaskStatus.setName("");
        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testTaskStatus));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        var request = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testTaskStatus));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdate() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        var updateTask = Map.of("name", faker.funnyName().name(), "slug", faker.internet().slug());
        var request = put("/api/task_statuses/{id}", testTaskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateTask));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var taskStatus = taskStatusRepository.findBySlug(updateTask.get("slug"));
        assertThat(taskStatus.isPresent()).isTrue();
        assertThat(taskStatus.get().getId()).isEqualTo(testTaskStatus.getId());
        assertThat(taskStatus.get().getName()).isEqualTo(updateTask.get("name")).isNotEqualTo(testTaskStatus.getName());
    }

    @Test
    public void testUpdatePartial() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        var updateTask = Map.of("slug", faker.internet().slug());
        var request = put("/api/task_statuses/{id}", testTaskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateTask));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var taskStatus = taskStatusRepository.findById(testTaskStatus.getId());
        assertThat(taskStatus.isPresent()).isTrue();
        assertThat(taskStatus.get().getSlug())
                .isEqualTo(updateTask.get("slug"))
                .isNotEqualTo(testTaskStatus.getSlug());
        assertThat(taskStatus.get().getName()).isEqualTo(testTaskStatus.getName());
    }

    @Test
    public void testUpdateNegative() throws Exception {
        var updateTask = Map.of("name", faker.funnyName().name(), "slug", faker.internet().slug());
        var request = put("/api/task_statuses/{id}", 999999)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateTask));

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateWithoutAuth() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        var updateTask = Map.of("name", faker.funnyName().name(), "slug", faker.internet().slug());
        var request = put("/api/task_statuses/{id}", testTaskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateTask));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDelete() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var request = delete("/api/task_statuses/{id}", testTaskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var taskStatus = taskStatusRepository.findById(testTaskStatus.getId());
        assertThat(taskStatus.isEmpty()).isTrue();
    }

    @Test
    public void testDeleteWithTask() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var testUser = userUtils.getTestUser();
        var task = Instancio.of(modelGenerator.getTaskModel()).create();
        task.setAssignee(testUser);
        task.setTaskStatus(testTaskStatus);
        taskRepository.save(task);

        var request = delete("/api/task_statuses/{id}", testTaskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isConflict());
    }

    @Test
    public void testDeleteWithoutAuth() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var request = delete("/api/task_statuses/{id}", testTaskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
