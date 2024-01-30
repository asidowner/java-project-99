package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelGenerator;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
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
    private UserMapper userMapper;

    @BeforeEach
    public void setUp() {
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
    }

    @Test
    public void testIndex() throws Exception {
        userRepository.save(testUser);
        var result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testShow() throws Exception {
        userRepository.save(testUser);

        var request = get("/api/users/{id}", testUser.getId());
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
        var request = get("/api/users/{id}", 99999);
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreate() throws Exception {
        var request = post("/api/users")
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
    public void testUpdateEmail() throws Exception {
        userRepository.save(testUser);

        var newEmail = faker.internet().emailAddress();

        var request = put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("email", newEmail)));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var user = userRepository.findByEmail(newEmail);

        assertThat(user.isPresent()).isTrue();
        assertThat(user.get().getEmail()).isEqualTo(newEmail).isNotEqualTo(testUser.getEmail());
        assertThat(user.get().getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(user.get().getLastName()).isEqualTo(testUser.getLastName());
    }

    @Test
    public void testUpdatePassword() throws Exception {
        userRepository.save(testUser);

        var newPassword = faker.internet().emailAddress();

        var request = put("/api/users/{id}", testUser.getId())
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
    public void testDelete() throws Exception {
        userRepository.save(testUser);


        var request = delete("/api/users/{id}", testUser.getId());

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var user = userRepository.findByEmail(testUser.getEmail());

        assertThat(user.isPresent()).isFalse();
    }
}
