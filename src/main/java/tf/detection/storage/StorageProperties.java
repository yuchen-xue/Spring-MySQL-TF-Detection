package tf.detection.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

	/**
	 * Folder uploadDirectory for storing files
	 */
	@Value("${storage.upload-directory}")
	private String uploadDirectory;

	@Value("${storage.result-directory}")
	private String resultDirectory;

	public String getUploadDirectory() {
		return uploadDirectory;
	}

	public void setUploadDirectory(String uploadDirectory) {
		this.uploadDirectory = uploadDirectory;
	}

	public String getResultDirectory() {
		return resultDirectory;
	}

	public void setResultDirectory(String resultDirectory) {
		this.resultDirectory = resultDirectory;
	}
}
