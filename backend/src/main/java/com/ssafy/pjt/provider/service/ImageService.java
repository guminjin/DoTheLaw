package com.ssafy.pjt.provider.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.pjt.core.repository.FileRepository;
import com.ssafy.pjt.core.service.dto.UploadFileDTO;
import com.ssafy.pjt.util.UploadFileUtils;

/**
 * 
 * @author https://github.com/woobong/spring-boot-jpa-summernote-image-upload-example/
 *
 */

@Service
public class ImageService {

	private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

	private final Path rootLocation;

	@Autowired
	public ImageService(String uploadPath) {
		logger.info("PATH :: " + uploadPath);
		this.rootLocation = Paths.get(uploadPath);
	}

	@Autowired
	FileRepository fileRepository;

	public Stream<Integer> loadAll() {
		List<UploadFileDTO> files = fileRepository.findAll();
		return files.stream().map(file -> file.getId());
	}

	public UploadFileDTO load(int fileId) {
		return fileRepository.findById(fileId).orElse(null);
	}

	public Resource loadAsResource(String fileName) throws Exception {
		try {
			if (fileName.toCharArray()[0] == '/') {
				fileName = fileName.substring(1);
			}
			
			Path file = loadPath(fileName);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new Exception("Could not read file: " + fileName);
			}
		} catch (Exception e) {
			throw new Exception("Could not read file: " + fileName);
		}
	}
	
	private Path loadPath(String fileName) {
		return rootLocation.resolve(fileName);
	}

	public UploadFileDTO store(MultipartFile file) throws Exception {
		try {
			if (file.isEmpty()) {
				throw new Exception("Failed to store empty file " + file.getOriginalFilename());
			}

			String saveFileName = UploadFileUtils.fileSave(rootLocation.toString(), file);

			if (saveFileName.toCharArray()[0] == '/') {
				saveFileName = saveFileName.substring(1);
			}

			Resource resource = loadAsResource(saveFileName);

			UploadFileDTO saveFile = new UploadFileDTO();
			saveFile.setSaveFileName(saveFileName);
			saveFile.setOriginFileName(file.getOriginalFilename());
			saveFile.setContentType(file.getContentType());
			saveFile.setFilePath(rootLocation.toString().replace(File.separatorChar, '/') + '/' + saveFileName);
			saveFile.setSize(resource.contentLength());
			saveFile.setRegDate(new Date());
			System.out.println(saveFile);
			saveFile = fileRepository.save(saveFile);

			return saveFile;
		} catch (IOException e) {
			throw new Exception("Failed to store file " + file.getOriginalFilename(), e);
		}
	}

	public UploadFileDTO store(MultipartFile file, String subPath, String filename, boolean thumbnail) throws Exception {
		try {
			if (file.isEmpty()) {
				throw new Exception("Failed to store empty file " + file.getOriginalFilename());
			}

			String saveFileName = UploadFileUtils.fileSave(rootLocation.toString(), subPath, filename, thumbnail, file);

			if (saveFileName.toCharArray()[0] == '/') {
				saveFileName = saveFileName.substring(1);
			}
			System.out.println(saveFileName);
			Resource resource = loadAsResource(saveFileName);

			UploadFileDTO saveFile = new UploadFileDTO();
			saveFile.setSaveFileName(saveFileName);
			saveFile.setOriginFileName(file.getOriginalFilename());
			saveFile.setContentType(file.getContentType());
			saveFile.setFilePath(rootLocation.toString().replace(File.separatorChar, '/') + '/' + saveFileName);
			saveFile.setSize(resource.contentLength());
			saveFile.setRegDate(new Date());
			System.out.println(saveFile);
			saveFile = fileRepository.save(saveFile);

			return saveFile;
		} catch (IOException e) {
			throw new Exception("Failed to store file " + file.getOriginalFilename(), e);
		}
	}
}