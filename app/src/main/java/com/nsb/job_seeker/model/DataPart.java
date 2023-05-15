package com.nsb.job_seeker.model;

import java.io.File;

public class DataPart {
    private String fileName;
    private byte[] content;
    private String type;

    public DataPart(String name, File fileCV) {
    }

    public DataPart(String name, byte[] data) {
        fileName = name;
        content = data;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

}
