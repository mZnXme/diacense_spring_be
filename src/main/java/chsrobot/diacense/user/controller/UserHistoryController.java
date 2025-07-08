package chsrobot.diacense.user.controller;

import chsrobot.diacense.user.UserHistoryDto;
import chsrobot.diacense.user.service.UserHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/users/history")
@RestController
public class UserHistoryController {

    private final UserHistoryService userHistoryService;

    public UserHistoryController(UserHistoryService userHistoryService) {
        this.userHistoryService = userHistoryService;
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addUserHistory(@RequestBody UserHistoryDto userHistoryDto) {
        userHistoryService.createNewHistory(userHistoryDto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "ข้อมูลถูกบันทึกเรียบร้อยแล้ว");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{days}")
    public ResponseEntity<?> getUserHistory(@PathVariable int days) {
        return ResponseEntity.ok(userHistoryService.getUserHistory(days));
    }
}
