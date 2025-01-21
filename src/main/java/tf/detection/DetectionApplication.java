package tf.detection;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.tensorflow.SavedModelBundle;

@SpringBootApplication
public class DetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DetectionApplication.class, args);
    }

    @Bean
    public SavedModelBundle loadDetectionModel(@Value("${tf.modelPath}") String tfModelPath) throws IOException {
        String detectionModelPath = ClassLoader.getSystemResource(tfModelPath).getPath();
        return SavedModelBundle.load(detectionModelPath, "serve");
    }

    @Bean
    public String[] loadDetectionLabels(@Value("${tf.labelPath}") String labelsPath) throws Exception {
        String detectionLabelPath = ClassLoader.getSystemResource(labelsPath).getPath();
        return loadLabels(detectionLabelPath);
    }

    @Bean
    public URL getInferenceFilePath(@Value("${tf.testImagePath}") String inferenceFilePath) {
        return ClassLoader.getSystemResource(inferenceFilePath);
    }

    private static String[] loadLabels(String filename) throws Exception {
        Path filePath = Paths.get(filename);
        List<String> lines = Files.lines(filePath).collect(Collectors.toList());
        String[] ret = lines.toArray(new String[0]);
        return ret;
    }

}
