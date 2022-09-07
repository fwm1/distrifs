package com.lf.distrifs.core.fs;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.lf.distrifs.common.Record;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public class FileRecord implements Record {

    private static final long serialVersionUID = 7825637577104627385L;

    String path;

    byte[] content;

    Operation operation;

    String md5 = "";

    public FileRecord(String path) {
        this.path = path;
    }

    @Override
    public String getChecksum() {
        if (Strings.isNullOrEmpty(md5) && content != null && content.length > 0) {
            try {
                md5 = ByteSource.wrap(content).hash(Hashing.md5()).toString();
            } catch (IOException ignored) {

            }
        }
        return md5;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileRecord that = (FileRecord) o;
        return Objects.equals(path, that.path) &&
                operation == that.operation &&
                Objects.equals(md5, that.md5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, operation, md5);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FileRecord.class.getSimpleName() + "[", "]")
                .add("path='" + path + "'")
                .add("operation=" + operation)
                .add("md5='" + md5 + "'")
                .toString();
    }

    enum Operation {
        NEW,
        DELETE,
        UPDATE
    }
}
