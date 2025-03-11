package datasahi.flow.commons.file;

import datasahi.flow.commons.error.GenericException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class FileUtil {

    public JSONObject readJsonFile(String filePath) {
        try {
            return new JSONObject(readFile(filePath));
        } catch (Exception e) {
            throw new GenericException("Failed to create json from file: " + filePath, e, 10);
        }
    }

    public String readFile(String filePath) {
        try {
            if (new File(filePath).exists()) {
                return new String((Files.readAllBytes(Paths.get(filePath))));
            }

            InputStream is = this.getClass().getResourceAsStream(filePath);
            if (is != null) {
                String data = new String(is.readAllBytes());
                is.close();
                return data;
            }
        } catch (IOException e) {
            throw new GenericException("Failed to read file: " + filePath, e, 10);
        }
        return null;
    }

    public InputStream getInputStream(String filepath) {

        try {
            if (new File(filepath).exists()) return new FileInputStream(filepath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found after exists check :: " + filepath, e);
        }

        return this.getClass().getResourceAsStream(filepath);
    }

    public String getFilePath(String workingDirPath, String filePath) {
        return createAndReturnDirPath(workingDirPath, filePath);
    }

    public String createAndReturnDirPath(String... dirArray) {
        return String.join("/", dirArray);
    }

    public void createParentDir(String path) {
        File parentFile = Paths.get(path).toFile().getParentFile();
        if (parentFile == null) {
            Paths.get(path).toFile().mkdirs();
            return;
        }
        parentFile.mkdirs();
    }

    public String createCompleteFilePath(String path, String filename) {
        return new StringBuilder()
                .append(path)
                .append(File.separator)
                .append(filename)
                .toString();
    }

    public void copyURLToFile(URL fileUrl, Path filePath) {
        try (InputStream inputStream = fileUrl.openStream()) {
            filePath.toFile().mkdirs();
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new GenericException("Failed to download file from: " + fileUrl, e, 10);
        }
    }
}
