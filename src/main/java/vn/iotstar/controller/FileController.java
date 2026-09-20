package vn.iotstar.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.iotstar.service.IStorageService;

@RestController
@RequestMapping("/uploads")
public class FileController {
    private final IStorageService storageService;

    public FileController(IStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaTypeFactory.getMediaType(file.getFilename())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(mediaType).body(file);
    }
}
