package chsrobot.diacense.user.model.audit;

import chsrobot.diacense.user.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.PreUpdate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserAuditListener {
    @PreUpdate
    public void preUpdate(User user) {
        try {
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            String dirPath = "logs/update/user/user_audit";
            String filePath = dirPath + "/user_audit_" + date + ".log";

            File dir = new File(dirPath);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + dirPath);
                }
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            String json = mapper.writeValueAsString(user);

            try (FileWriter writer = new FileWriter(filePath, true)) {
                writer.write(json + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

