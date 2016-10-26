package com.sensordc;

public class DataFileInfo {
    public String fileName;
    public long fileSize;

    public DataFileInfo(String fileName, long size) {
        this.fileName = fileName;
        this.fileSize = size;
    }


    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof DataFileInfo) {
            DataFileInfo that = (DataFileInfo) other;
            if (this.fileName.equals(that.fileName) && this.fileSize == that.fileSize)
                result = true;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return (int) (41 * (41 + fileName.hashCode()) + fileSize);
    }
}
