package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LabelControllerTest {
    private Label testLabel;

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
    private Faker faker;

    @Autowired
    private LabelRepository labelRepository;


    @BeforeEach
    public void setUp() {
        testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        token = jwt().jwt(builder -> builder.subject(userUtils.getTestUser().getEmail()));
    }

    @Test
    public void testIndex() throws Exception {
        labelRepository.save(testLabel);
        var result = mockMvc.perform(get("/api/labels").with(token))
                .andExpect(status().isOk())
                .andReturn();


        var totalCount = result.getResponse().getHeader("X-Total-Count");

        assertThat(totalCount).isNotNull();
        assertThat(Long.valueOf(totalCount)).isEqualTo(labelRepository.count());

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testIndexOrderAndPaging() throws Exception {
        var start = 10;
        var end = 20;
        var order = "DESC";
        var sort = "id";

        var request = get("/api/labels")
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
        labelRepository.save(testLabel);
        mockMvc.perform(get("/api/labels"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testShow() throws Exception {
        labelRepository.save(testLabel);
        var request = get("/api/labels/{id}", testLabel.getId()).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testLabel.getId()),
                v -> v.node("name").isEqualTo(testLabel.getName()),
                v -> v.node("createdAt").isPresent()
        );
    }

    @Test
    public void testShowNegative() throws Exception {
        var request = get("/api/labels/{id}", 99999).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testShowWithoutAuth() throws Exception {
        labelRepository.save(testLabel);
        var request = get("/api/labels/{id}", testLabel.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreate() throws Exception {
        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testLabel));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var label = labelRepository.findByName(testLabel.getName());

        assertThat(label.isPresent()).isTrue();
        assertThat(label.get().getCreatedAt()).isNotNull();
    }

    @Test
    public void testCreateNegative() throws Exception {
        var labelCreateRequest = Map.of(
                "name", "ab"
        );

        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(labelCreateRequest));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateUnique() throws Exception {
        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testLabel));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var secondRequest = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testLabel));

        mockMvc.perform(secondRequest)
                .andExpect(status().isConflict());
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        var request = post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testLabel));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdate() throws Exception {
        labelRepository.save(testLabel);

        var newName = faker.text().text(3, 1000);

        var request = put("/api/labels/{id}", testLabel.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("name", newName)));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var label = labelRepository.findByName(newName);
        var oldLabel = labelRepository.findByName(testLabel.getName());

        assertThat(label.isPresent()).isTrue();
        assertThat(oldLabel.isEmpty()).isTrue();
    }

    @Test
    public void testUpdateNegative() throws Exception {
        labelRepository.save(testLabel);

        var newName = "ab";

        var request = put("/api/labels/{id}", testLabel.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("name", newName)));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateWithoutAuth() throws Exception {
        labelRepository.save(testLabel);

        var newName = faker.text().text(3, 1000);

        var request = put("/api/labels/{id}", testLabel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("name", newName)));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDelete() throws Exception {
        labelRepository.save(testLabel);

        var request = delete("/api/labels/{id}", testLabel.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var label = labelRepository.findById(testLabel.getId());
        assertThat(label.isEmpty()).isTrue();
    }

    @Test
    public void testDeleteIfExistsTaskWithLabel() throws Exception {
//        ToDo
    }

    @Test
    public void testDeleteWithoutAuth() throws Exception {
        labelRepository.save(testLabel);


        var request = delete("/api/labels/{id}", testLabel.getId())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
