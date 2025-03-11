package datasahi.flow.eval;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FieldExtractorTest {

    @Test
    public void testFieldExtractor() {
        FieldExtractor extractor = new FieldExtractor();
        String expression = "status == 'DONE' || user == 'admin'";
        List<String> fields = extractor.extractFields(expression);
        assertEquals(2, fields.size());
        assertTrue(fields.contains("status"));
        assertTrue(fields.contains("user"));
    }

}
