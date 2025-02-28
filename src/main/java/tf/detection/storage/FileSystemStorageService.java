package tf.detection.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

	// Name of the directory that stores the user-uploaded images.
	private final Path uploadDirectory;

	// Name of the directory that stores the images with detection bounding boxes.
	private final Path resultDirectory;

	// The lasest user-uploaded image.
	private Path uploadFile;

	// The lasest image with detection bounding boxes.
	private Path resultFile;

	public FileSystemStorageService(StorageProperties properties) {
        
        if(properties.getUploadDirectory().trim().length() == 0){
            throw new StorageException("File upload location can not be Empty."); 
        }
        
        if(properties.getResultDirectory().trim().length() == 0){
            throw new StorageException("Result location can not be Empty."); 
        }

		this.uploadDirectory = Paths.get(properties.getUploadDirectory());
		this.resultDirectory = Paths.get(properties.getResultDirectory());
	}

	@Override
	public Path getLatestUploadFile() {
		return this.uploadFile;
	}

	@Override
	public Path getLatestResultFile() {
		return this.resultFile;
	}

	@Override
	public void store(MultipartFile multipartFile) {

		// Update the location of the uploaded image.
		// This file shall be created by the end of this method call.
		this.uploadFile = this.uploadDirectory.resolve(
			Paths.get(multipartFile.getOriginalFilename()))
			.normalize().toAbsolutePath();

		// Update the location of the image with detection result.
		// This file is not existent until the end of this method call.
		this.resultFile = this.resultDirectory.resolve(
			Paths.get(multipartFile.getOriginalFilename()))
			.normalize().toAbsolutePath();

			try {
			if (multipartFile.isEmpty()) {
				throw new StorageException("Failed to store empty file.");
			}
			if (!uploadFile.getParent().equals(this.uploadDirectory.toAbsolutePath())) {
				// This is a security check
				throw new StorageException(
						"Cannot store file outside current directory.");
			}
			try (InputStream inputStream = multipartFile.getInputStream()) {
				Files.copy(inputStream, uploadFile,
					StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file.", e);
		}
	}

	private Stream<Path> loadAll(Path directory) {
		/*
		 * Return a stream of all files in the result directory.
		 */
		 try {
			return Files.walk(directory, 1)
				.filter(path -> !path.equals(directory))
				.map(directory::relativize);
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}
	}

	@Override
	public Stream<Path> loadAllUpload() {
		return loadAll(this.uploadDirectory);
	}

	@Override
	public Stream<Path> loadAllResult() {
		return loadAll(this.resultDirectory);
	}

	@Override
	public Path loadUpload(String filename) {
		return uploadDirectory.resolve(filename);
	}

	@Override
	public Path loadResult(String filename) {
		return resultDirectory.resolve(filename);
	}

	private Resource loadAsResource(String filename, String directory) {
		try {
			Path file;
			switch(directory) {
				case "upload" -> file = loadUpload(filename);
				case "result" -> file = loadResult(filename);
				default -> throw new IllegalArgumentException("Invalid condition. Should be either 'upload' or 'result'.");
			}
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			}
			else {
				throw new StorageFileNotFoundException(
					"Could not read file: " + filename);
				}
			}
			catch (MalformedURLException e) {
				throw new StorageFileNotFoundException("Could not read file: " + filename, e);
			}
		}

	@Override
	public Resource loadUploadResource(String filename) {
		return loadAsResource(filename, "upload");
	} 

	@Override
	public Resource loadResultResource(String filename) {
		return loadAsResource(filename, "result");
	} 

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(uploadDirectory.toFile());
		FileSystemUtils.deleteRecursively(resultDirectory.toFile());
	}

	@Override
	public void init() {
		try {
			Files.createDirectories(uploadDirectory);
			Files.createDirectories(resultDirectory);
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}
}
