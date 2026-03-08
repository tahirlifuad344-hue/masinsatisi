package com.example.masinsatisi.storage;

import com.example.masinsatisi.config.AppProperties;
import com.example.masinsatisi.exception.BadRequestException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private final AppProperties appProperties;

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is empty");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "_" + originalName;

        Path uploadDir = Paths.get(appProperties.getUpload().getDir()).toAbsolutePath().normalize();
        Path targetLocation = uploadDir.resolve(fileName);

        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BadRequestException("Could not store image file");
        }

        String url = "/uploads/" + fileName;
        return new StoredFile(
                fileName,
                targetLocation.toString(),
                url,
                file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                file.getSize());
    }

    public void deleteIfExists(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException ex) {
            // Ignore delete failures for now
        }
    }
}
