package com.mac;

public class S3EventModel {

    String bucketName;
    String objectKey;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    @Override
    public String toString() {
        return "S3EventModel{" +
                "bucketName='" + bucketName + '\'' +
                ", objectKey='" + objectKey + '\'' +
                '}';
    }
}
