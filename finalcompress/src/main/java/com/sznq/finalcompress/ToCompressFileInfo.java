package com.sznq.finalcompress;

import java.io.File;
import java.util.Objects;

public class ToCompressFileInfo {

    public File file;
    public long startTime;
    public long pastTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ToCompressFileInfo)) return false;
        ToCompressFileInfo fileInfo = (ToCompressFileInfo) o;
        return file.getAbsolutePath().equals(fileInfo.file.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        return file.getAbsolutePath().hashCode()+90;
    }
}
