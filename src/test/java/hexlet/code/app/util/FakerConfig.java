package hexlet.code.app.util;

import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class FakerConfig {
    @Bean
    public Faker getFaker() {
        return new Faker(new Locale("en", "US"));
    }
}
