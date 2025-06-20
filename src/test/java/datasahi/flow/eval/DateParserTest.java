package datasahi.flow.eval;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class DateParserTest {

    @Test
    public void testParse() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Now :: " + now.toString());
        String updatedTime = "2025-06-18T11:30:43.151";
        LocalDateTime parsedDate = LocalDateTime.parse(updatedTime);
        System.out.println(parsedDate);
    }
}
