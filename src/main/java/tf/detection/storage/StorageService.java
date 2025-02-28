package tf.detection.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;


public interface StorageService {

	void init();

	void store(MultipartFile file);

	Path getLatestUploadFile();

	Path getLatestResultFile();

	Stream<Path> loadAllUpload();

	Stream<Path> loadAllResult();

	Path loadUpload(String filename);

	Path loadResult(String filename);

	Resource loadUploadResource(String filename);

	Resource loadResultResource(String filename);

	void deleteAll();

}
