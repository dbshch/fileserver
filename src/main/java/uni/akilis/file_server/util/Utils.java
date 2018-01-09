package uni.akilis.file_server.util;


import java.util.Map;

/**
 * Created by leo on 11/11/17.
 */
public class Utils {
    public static String mapToString(Map<String, String[]> map) {
        if (map == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> ent: map.entrySet()) {
            sb.append(ent.getKey()).append(" = ");
            for (String str: ent.getValue()) {
                sb.append(str).append(", ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
