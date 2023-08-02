package dev.crasher508.permssystem.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Language {

    private final HashMap<String, String> translationMap = new HashMap<>();
    private final File file;

    public Language(String fileName) {
        this.file = new File(fileName);
        this.load();
    }

    public Language(File file) {
        this.file = file;
        this.load();
    }

    //Thanks to WDPE Team
    private void load() {
        try {
            if (!this.file.exists() || this.file.isDirectory()) {
                throw new FileNotFoundException();
            }
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(this.file), StandardCharsets.UTF_8))) {
                String temp;
                temp = br.readLine();
                while (temp != null) {
                    if (stringBuilder.length() != 0) {
                        stringBuilder.append("\n");
                    }
                    stringBuilder.append(temp);
                    temp = br.readLine();
                }
            }
            for (String line : stringBuilder.toString().split("\n")) {
                line = line.trim();
                if (line.equals("") || line.charAt(0) == '#') {
                    continue;
                }
                String[] t = line.split("=");
                if (t.length < 2) {
                    continue;
                }
                String key = t[0];
                StringBuilder value = new StringBuilder();
                for (int i = 1; i < t.length - 1; i++) {
                    value.append(t[i]).append("=");
                }
                value.append(t[t.length - 1]);
                if (value.toString().equals("")) {
                    continue;
                }
                this.translationMap.put(key.trim(), value.toString().trim());
            }
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
        }
    }

    public String get(String key) {
        String translation = this.translationMap.get(key);
        if (translation == null)
            return key;
        return translation;
    }

    public String translateString(String key, String... args) {
        String string = this.get(key);
        if (string.equals(key))
            return string;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                string = string.replace("{%" + i + "}", String.valueOf(args[i]));
            }
        }
        return string;
    }
}
