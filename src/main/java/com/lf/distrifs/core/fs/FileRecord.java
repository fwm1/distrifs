package com.lf.distrifs.core.fs;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.lf.distrifs.common.Record;

import java.io.File;
import java.io.IOException;

public class FileRecord implements Record {

    private static final long serialVersionUID = 7825637577104627385L;

    File file;

    String md5 = "";

    public FileRecord(File file) {
        this.file = file;
    }

    @Override
    public String getChecksum() {
        if (Strings.isNullOrEmpty(md5) && file != null && file.exists()) {
            try {
                md5 = Files.asByteSource(file).hash(Hashing.md5()).toString();
            } catch (IOException ignored) {

            }
        }
        return md5;
    }
}
