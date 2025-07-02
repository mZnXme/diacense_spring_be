package chsrobot.diacense.security.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain=true)
public class LoginResponse {
    private String token;
    private long expiresIn;
}
