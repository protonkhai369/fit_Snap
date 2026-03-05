package fitspan.file_service.service;


import fitspan.file_service.dto.FileInfo;
import fitspan.file_service.dto.response.FileData;
import fitspan.file_service.dto.response.FileMgmtResponse;
import fitspan.file_service.mapper.FileMgmtMapper;
import fitspan.file_service.repository.FileMgmtRepository;
import fitspan.file_service.repository.FileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {
    FileMgmtRepository fileMgmtRepository;
    FileRepository fileRepository;
    FileMgmtMapper mapper;

    public FileMgmtResponse upload(MultipartFile multipartFile) throws IOException {
        FileInfo fileInfo =fileRepository.store(multipartFile);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        //Create file
        var fileMgmt = mapper.toFileMgmt(fileInfo);
        fileMgmt.setOwnedID(userId);
        fileMgmt = fileMgmtRepository.save(fileMgmt);
        return FileMgmtResponse.builder()
                .originalFileName(multipartFile.getOriginalFilename())
                .url(fileInfo.getUrl())
                .build();
    }
    public FileData download (String fileName) throws IOException {
        var fileMgmt = fileMgmtRepository.findById(fileName)
                .orElseThrow(()->new RuntimeException("File not found"));
        var resource = fileRepository.read(fileMgmt);
        return new FileData(fileMgmt.getContentType(), resource);
    }
}
