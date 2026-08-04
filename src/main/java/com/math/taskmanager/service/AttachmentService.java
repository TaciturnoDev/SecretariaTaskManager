package com.math.taskmanager.service;

import com.math.taskmanager.entity.Attachment;
import com.math.taskmanager.entity.TaskHistory;
import com.math.taskmanager.repository.AttachmentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.time.LocalDateTime;
import java.util.UUID;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentService {

private final AttachmentRepository attachmentRepository;

@Value("${app.upload.base-path}")
private String uploadBasePath;

/**
 * =====================================================
 * Salvar anexo
 * =====================================================
 */
public Attachment saveAttachment(
        MultipartFile file,
        TaskHistory history
) throws IOException {

    if (file == null || file.isEmpty()) {

        throw new IllegalArgumentException(
                "Arquivo vazio."
        );
    }

    String relativePath = "2026/S1";

    Path uploadDirectory =
            Paths.get(
                    uploadBasePath,
                    relativePath
            );

    Files.createDirectories(
            uploadDirectory
    );

    String originalName =
            file.getOriginalFilename();

    if (originalName == null
            || originalName.isBlank()) {

        throw new IllegalArgumentException(
                "Nome de arquivo inválido."
        );
    }

    int dotIndex =
            originalName.lastIndexOf('.');

    String extension = "";

    if (dotIndex > 0) {

        extension =
                originalName.substring(
                        dotIndex
                );
    }

    String baseName =
            dotIndex > 0
                    ? originalName.substring(
                            0,
                            dotIndex
                    )
                    : originalName;

    baseName =
            baseName.replaceAll(
                    "[^a-zA-Z0-9_-]",
                    "_"
            );

    String uniqueId =
            UUID.randomUUID()
                    .toString()
                    .substring(0, 5);

    String storedFileName =
            baseName
                    + "_"
                    + uniqueId
                    + extension;

    Path targetFile =
            uploadDirectory.resolve(
                    storedFileName
            );

    Files.copy(
            file.getInputStream(),
            targetFile,
            StandardCopyOption.REPLACE_EXISTING
    );

    Attachment attachment =
            Attachment.builder()

                    .history(history)

                    .originalFileName(
                            originalName
                    )

                    .storedFileName(
                            storedFileName
                    )

                    .filePath(
                            relativePath
                                    + "/"
                                    + storedFileName
                    )

                    .fileSize(
                            file.getSize()
                    )

                    .contentType(
                            file.getContentType()
                    )

                    .active(true)

                    .uploadedAt(
                            LocalDateTime.now()
                    )

                    .build();

    return attachmentRepository.save(
            attachment
    );
}

/**
 * =====================================================
 * Buscar caminho físico do anexo
 * =====================================================
 */
public Path getAttachmentPath(
        Long attachmentId
) {

    Attachment attachment =
            attachmentRepository.findById(
                    attachmentId
            )
            .orElseThrow(() ->
                    new RuntimeException(
                            "Anexo não encontrado."
                    )
            );

    if (!Boolean.TRUE.equals(
            attachment.getActive()
    )) {

        throw new RuntimeException(
                "Anexo desativado."
        );
    }

    Path path =
            Paths.get(
                    uploadBasePath,
                    attachment.getFilePath()
            );

    if (!Files.exists(path)) {

        throw new RuntimeException(
                "Arquivo físico não encontrado."
        );
    }

    return path;
}

/**
 * =====================================================
 * Buscar anexo por ID
 * =====================================================
 */
public Attachment findById(
        Long attachmentId
) {

    return attachmentRepository.findById(
            attachmentId
    )
    .orElseThrow(() ->
            new RuntimeException(
                    "Anexo não encontrado."
            )
    );
}

/**
 * =====================================================
 * Soft Delete do anexo
 * =====================================================
 */
public Attachment deactivateAttachment(
        Long attachmentId
) {

    Attachment attachment =
            attachmentRepository.findById(
                    attachmentId
            )
            .orElseThrow(() ->
                    new RuntimeException(
                            "Anexo não encontrado."
                    )
            );

    attachment.setActive(false);

    return attachmentRepository.save(
            attachment
    );
}


/* documentação */

public List<Attachment> findByTask(Long taskId) {

    return attachmentRepository
            .findByHistoryTaskIdAndActiveTrueOrderByUploadedAtDesc(taskId);
}


}
