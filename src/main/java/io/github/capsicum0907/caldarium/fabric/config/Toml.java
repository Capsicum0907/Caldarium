package io.github.capsicum0907.caldarium.fabric.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class Toml {
    private Toml() {
    }

    static Map<String, Object> read(String text) {
        Map<String, Object> out = new LinkedHashMap<>();
        String table = "";
        StringBuilder pending = null;
        String pendingKey = null;
        for (String raw : text.split("\r?\n")) {
            String line = stripComment(raw).trim();
            if (pending != null) {
                pending.append(' ').append(line);
                if (balanced(pending)) {
                    out.put(pendingKey, value(pending.toString().trim()));
                    pending = null;
                }
                continue;
            }
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                table = line.substring(1, line.length() - 1).trim();
                continue;
            }
            int equals = line.indexOf('=');
            if (equals < 0) {
                continue;
            }
            String key = unquote(line.substring(0, equals).trim());
            String full = table.isEmpty() ? key : table + "." + key;
            String rest = line.substring(equals + 1).trim();
            if (rest.startsWith("[") && !balanced(new StringBuilder(rest))) {
                pending = new StringBuilder(rest);
                pendingKey = full;
                continue;
            }
            out.put(full, value(rest));
        }
        return out;
    }

    static String write(List<ModConfigSpec.ConfigValue<?>> values) {
        Map<String, List<ModConfigSpec.ConfigValue<?>>> tables = new LinkedHashMap<>();
        for (ModConfigSpec.ConfigValue<?> value : values) {
            List<String> path = value.path();
            String table = String.join(".", path.subList(0, path.size() - 1));
            tables.computeIfAbsent(table, ignored -> new ArrayList<>()).add(value);
        }
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, List<ModConfigSpec.ConfigValue<?>>> table : tables.entrySet()) {
            if (out.length() > 0) {
                out.append('\n');
            }
            if (!table.getKey().isEmpty()) {
                out.append('[').append(table.getKey()).append("]\n");
            }
            for (ModConfigSpec.ConfigValue<?> value : table.getValue()) {
                List<String> path = value.path();
                out.append(path.get(path.size() - 1)).append(" = ").append(literal(value.get())).append('\n');
            }
        }
        return out.toString();
    }

    private static String literal(Object value) {
        if (value instanceof String text) {
            return '"' + text.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
        }
        if (value instanceof List<?> list) {
            List<String> parts = new ArrayList<>();
            for (Object element : list) {
                parts.add(literal(element));
            }
            return "[" + String.join(", ", parts) + "]";
        }
        if (value instanceof Double real) {
            return String.format(Locale.ROOT, "%s", real);
        }
        return String.valueOf(value);
    }

    private static Object value(String text) {
        if (text.startsWith("\"")) {
            return unquote(text);
        }
        if (text.startsWith("[")) {
            List<Object> list = new ArrayList<>();
            for (String part : split(text.substring(1, text.lastIndexOf(']')))) {
                if (!part.isEmpty()) {
                    list.add(value(part));
                }
            }
            return list;
        }
        if (text.equals("true") || text.equals("false")) {
            return Boolean.parseBoolean(text);
        }
        String number = text.replace("_", "");
        try {
            return Long.parseLong(number);
        } catch (NumberFormatException notWhole) {
            try {
                return Double.parseDouble(number);
            } catch (NumberFormatException notReal) {
                return text;
            }
        }
    }

    private static List<String> split(String inside) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < inside.length(); i++) {
            char c = inside.charAt(i);
            if (c == '"' && (i == 0 || inside.charAt(i - 1) != '\\')) {
                quoted = !quoted;
            }
            if (c == ',' && !quoted) {
                parts.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString().trim());
        return parts;
    }

    private static String unquote(String text) {
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return text;
    }

    private static String stripComment(String line) {
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"' && (i == 0 || line.charAt(i - 1) != '\\')) {
                quoted = !quoted;
            }
            if (c == '#' && !quoted) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static boolean balanced(CharSequence text) {
        int depth = 0;
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' && (i == 0 || text.charAt(i - 1) != '\\')) {
                quoted = !quoted;
            } else if (!quoted && c == '[') {
                depth++;
            } else if (!quoted && c == ']') {
                depth--;
            }
        }
        return depth <= 0;
    }
}
