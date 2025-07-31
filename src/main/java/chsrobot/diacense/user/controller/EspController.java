package chsrobot.diacense.user.controller;

import chsrobot.diacense.user.EspHistoryDto;
import chsrobot.diacense.user.UserHistoryDto;
import chsrobot.diacense.user.model.HistoryType;
import chsrobot.diacense.user.service.UserHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/esp")
@RestController
public class EspController {
    private final UserHistoryService userHistoryService;

    public EspController(UserHistoryService userHistoryService) {
        this.userHistoryService = userHistoryService;
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addUserHistory(@RequestBody EspHistoryDto espHistoryDto) {
        if (espHistoryDto.getUsername() == null && espHistoryDto.getType() == null && espHistoryDto.getValue() == 0) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid input data");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (espHistoryDto.getValue() < 0) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Value must be non-negative");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        assert espHistoryDto.getType() != null;
        if (!espHistoryDto.getType().equals(HistoryType.BLOOD_GLUCOSE) && !espHistoryDto.getType().equals(HistoryType.BREATH_ACETONE)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid type");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            userHistoryService.addNewHistoryByEsp(espHistoryDto);
            Map<String, String> response = new HashMap<>();
            response.put("message", "successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

