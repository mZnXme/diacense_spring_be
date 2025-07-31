package chsrobot.diacense.user;

import chsrobot.diacense.user.model.HistoryType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EspHistoryDto {
    private String username;
    private HistoryType type;
    private double value;
}
