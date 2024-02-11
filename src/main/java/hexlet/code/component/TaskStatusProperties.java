package hexlet.code.component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "task")
@Getter
@Setter
class TaskStatusProperties {
    private List<Map<String, String>> defaultStatuses;
}
