package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LocalStorageService {

    @Value("${storage.local.path}")
    private String basePath;

    public String save(byte[] data, String fileName) throws IOException {
        Path dir = Path.of(basePath);
        Files.createDirectories(dir);

        Path file = dir.resolve(fileName);
        Files.write(file, data);

        return file.toAbsolutePath().toString();
    }
}