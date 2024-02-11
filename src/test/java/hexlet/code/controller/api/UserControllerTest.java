package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
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
class UserControllerTest {
    private User testUser;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Faker faker;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        token = jwt().jwt(builder -> builder.subject(userUtils.getTestUser().getEmail()));
    }

    @Test
    public void testIndex() throws Exception {
        userRepository.save(testUser);
        var result = mockMvc.perform(get("/api/users").with(token))
                .andExpect(status().isOk())
                .andReturn();

        var totalCount = result.getResponse().getHeader("X-Total-Count");

        assertThat(totalCount).isNotNull();
        assertThat(Long.valueOf(totalCount)).isEqualTo(userRepository.count());

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testIndexOrderAndPaging() throws Exception {
        var start = 10;
        var end = 20;
        var order = "DESC";
        var sort = "id";

        var request = get("/api/users")
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
    public void testShow() throws Exception {
        userRepository.save(testUser);

        var request = get("/api/users/{id}", testUser.getId()).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()),
                v -> v.node("email").isEqualTo(testUser.getEmail()),
                v -> v.node("createdAt").isPresent(),
                v -> v.node("password").isAbsent()
        );
    }

    @Test
    public void testShowNegative() throws Exception {
        var request = get("/api/users/{id}", 99999).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreate() throws Exception {
        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testUser));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(testUser.getEmail());

        assertThat(user.isPresent()).isTrue();
        assertThat(user.get().getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(user.get().getLastName()).isEqualTo(testUser.getLastName());
        assertThat(user.get().getPassword()).isNotEqualTo(testUser.getPassword());
        assertThat(user.get().getCreatedAt()).isNotNull();
    }

    @Test
    public void testUpdate() throws Exception {
        userRepository.save(testUser);

        var testToken = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        var newFirstName = faker.name().firstName();
        var newLastName = faker.name().lastName();
        var newEmail = faker.internet().emailAddress();

        var requestData = Map.of("email", newEmail, "firstName", newFirstName, "lastName", newLastName);

        var request = put("/api/users/{id}", testUser.getId())
                .with(testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(requestData));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var user = userRepository.findByEmail(newEmail);

        assertThat(user.isPresent()).isTrue();
        assertThat(user.get().getEmail()).isEqualTo(newEmail).isNotEqualTo(testUser.getEmail());
        assertThat(user.get().getFirstName()).isEqualTo(newFirstName).isNotEqualTo(testUser.getFirstName());
        assertThat(user.get().getLastName()).isEqualTo(newLastName).isNotEqualTo(testUser.getLastName());
    }

    @Test
    public void testUpdatePassword() throws Exception {
        userRepository.save(testUser);

        var testToken = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        var newPassword = faker.internet().emailAddress();

        var request = put("/api/users/{id}", testUser.getId())
                .with(testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("password", newPassword)));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var user = userRepository.findByEmail(testUser.getEmail());

        assertThat(user.isPresent()).isTrue();
        assertThat(user.get().getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(user.get().getLastName()).isEqualTo(testUser.getLastName());
        assertThat(user.get().getPassword()).isNotEqualTo(testUser.getPassword());
    }

    @Test
    public void testUpdateByOtherUser() throws Exception {
        userRepository.save(testUser);

        var newEmail = faker.internet().emailAddress();
        var newPassword = faker.internet().emailAddress();

        var request = put("/api/users/{id}", testUser.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("password", newPassword, "email", newEmail)));

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDelete() throws Exception {
        userRepository.save(testUser);

        var testToken = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        var request = delete("/api/users/{id}", testUser.getId()).with(testToken);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var user = userRepository.findByEmail(testUser.getEmail());

        assertThat(user.isPresent()).isFalse();
    }

    @Test
    public void testDeleteByOtherUser() throws Exception {
        userRepository.save(testUser);

        var request = delete("/api/users/{id}", testUser.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteWithTask() throws Exception {
        userRepository.save(testUser);

        var testToken = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        var taskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(taskStatus);

        var task = Instancio.of(modelGenerator.getTaskModel()).create();
        task.setAssignee(testUser);
        task.setTaskStatus(taskStatus);
        taskRepository.save(task);

        var request = delete("/api/users/{id}", testUser.getId())
                .with(testToken)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isConflict());
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {
        userRepository.save(testUser);
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

    }

    @Test
    public void testShowWithoutAuth() throws Exception {
        userRepository.save(testUser);
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isUnauthorized());

    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(testUser)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateWithoutAuth() throws Exception {
        userRepository.save(testUser);

        var newEmail = faker.internet().emailAddress();
        var newPassword = faker.internet().emailAddress();

        var request = put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("password", newPassword, "email", newEmail)));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteWithoutAuth() throws Exception {
        userRepository.save(testUser);

        var request = delete("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
