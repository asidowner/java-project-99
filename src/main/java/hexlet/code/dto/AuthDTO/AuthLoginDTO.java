package hexlet.code.dto.AuthDTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthLoginDTO {
    private String username;
    private String password;
}
