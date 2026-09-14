package project.shared.util;

import java.util.HashMap;
import java.util.Map;

public final class ResponseParser {
    private ResponseParser() {
    }

    public static Map<String, String> parseRowData(String row) {
        Map<String, String> data = new HashMap<>();
        if (row == null || row.isBlank()) {
            return data;
        }

        String[] fields = row.split("\\|");
        for (String field : fields) {
            String[] kv = field.split("=", 2);
            if (kv.length == 2) {
                data.put(kv[0].trim(), kv[1].trim());
            }
        }
        return data;
    }
}
