package chsrobot.diacense.user.controller;

import chsrobot.diacense.user.EspHistoryDto;
import chsrobot.diacense.user.UserHistoryDto;
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

