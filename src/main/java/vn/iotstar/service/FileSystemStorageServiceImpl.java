package vn.iotstar.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.config.StorageProperties;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileSystemStorageServiceImpl implements IStorageService {
    private final Path rootLocation;

    public FileSystemStorageServiceImpl(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation()).toAbsolutePath().normalize();
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo thư mục upload", e);
        }
    }

    @Override
    public String getStorageFilename(MultipartFile file, String id) {
        String original = file.getOriginalFilename();
        String extension = "";
        if (original != null && original.lastIndexOf('.') >= 0) {
            extension = original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        return id + extension;
    }

    @Override
    public void store(MultipartFile file, String storeFilename) {
        if (file == null || file.isEmpty()) {
            return;
        }
        try {
            Path destination = rootLocation.resolve(Paths.get(storeFilename)).normalize();
            if (!destination.getParent().equals(rootLocation)) {
                throw new RuntimeException("Tên file không hợp lệ");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể lưu file", e);
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename).normalize();
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Resource resource = new UrlResource(load(filename).toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public String newId() {
        return UUID.randomUUID().toString();
    }
}
