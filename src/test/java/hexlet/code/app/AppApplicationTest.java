package hexlet.code.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testStatic() throws Exception {
        var response = mockMvc.perform(get("/index.html").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andReturn();

        var body = response.getResponse().getContentAsString();
        assertThat(body).isNotBlank();
    }

}
