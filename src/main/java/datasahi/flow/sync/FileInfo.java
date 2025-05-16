package datasahi.flow.sync;

import java.time.Instant;
import java.util.Objects;

public class FileInfo {
    private final String filePath;     // Full path of the file
    private final String fileName;     // Name of the file
    private final boolean isDirectory; // Whether it's a directory
    private final long sizeInBytes;    // Size of the file in bytes
    private final Instant dateCreated; // Creation timestamp
    private final Instant dateModified; // Last modification timestamp
    private final String id;
    private String relativePath;     // path as seen from the source ow working folder
    private String archivePath;
    private String errorPath;

    // Constructor
    public FileInfo(String filePath, String fileName, boolean isDirectory,
                    long sizeInBytes, Instant dateCreated, Instant dateModified) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.isDirectory = isDirectory;
        this.sizeInBytes = sizeInBytes;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
        this.id = (filePath + ";" + dateCreated.toString());
    }

    public String getId() {
        return id;
    }

    // Getters
    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public Instant getDateModified() {
        return dateModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileInfo fileInfo = (FileInfo) o;
        return Objects.equals(filePath, fileInfo.filePath) && Objects.equals(dateCreated, fileInfo.dateCreated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, dateCreated);
    }

    public String getRelativePath() {
        return relativePath;
    }

    public FileInfo setRelativePath(String relativePath) {
        this.relativePath = relativePath;
        return this;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", isDirectory=" + isDirectory +
                ", sizeInBytes=" + sizeInBytes +
                ", dateCreated=" + dateCreated +
                ", dateModified=" + dateModified +
                '}';
    }
}