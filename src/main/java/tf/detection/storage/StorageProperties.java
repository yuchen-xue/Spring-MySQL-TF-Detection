package tf.detection.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

	/**
	 * Folder uploadDirectory for storing files
	 */
	private String uploadDirectory = "upload-dir";
	private String resultDirectory = "result-dir";

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
