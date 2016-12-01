package com.sensordc;

class DataFileInfo {
    private final String fileName;
    private final long fileSize;

    DataFileInfo(String fileName, long size) {
        this.fileName = fileName;
        this.fileSize = size;
    }

    @Override
    public int hashCode() {
        return (int) (41 * (41 + this.fileName.hashCode()) + this.fileSize);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DataFileInfo) {
            DataFileInfo that = (DataFileInfo) other;
            if (this.fileName.equals(that.fileName) && this.fileSize == that.fileSize)
                return true;
        }
        return false;
    }
}
