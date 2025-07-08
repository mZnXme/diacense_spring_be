package chsrobot.diacense.user;

import chsrobot.diacense.user.model.HistoryType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserHistoryDto {
    private HistoryType type;
    private double value;
}
