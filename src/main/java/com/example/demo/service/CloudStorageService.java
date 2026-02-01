package com.example.demo.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CloudStorageService {

    private final Storage storage;

    @Value("${gcs.bucket}")
    private String bucket;

    public CloudStorageService(Storage storage) {
        this.storage = storage;
    }

    public String upload(byte[] data, String fileName) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucket, fileName).build();
        storage.create(blobInfo, data);

        return String.format("https://storage.googleapis.com/%s/%s", bucket, fileName);
    }
}