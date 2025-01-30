package tf.detection;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationContext;

import org.tensorflow.Graph;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.op.Ops;

import tf.detection.detection.DetectionResultParser;
import tf.detection.storage.StorageService;
import tf.detection.storage.StorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class DetectionApplication {

    @Autowired
    private ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(DetectionApplication.class, args);
    }

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			storageService.deleteAll();
			storageService.init();
		};
	}

	@Bean(name = "model")
	SavedModelBundle loadDetectionModel() {
		// get path to model folder
		String modelPath = ClassLoader.getSystemResource("tf_inception/ssd_mobilenet_v2_fpnlite_320x320-saved_model").getPath();
		// load saved model
		return SavedModelBundle.load(modelPath, "serve");
	}

	// Initialize a TF computational graph.
	@Bean(name = "graph")
	Graph getGraph() {
		return new Graph();
	}
	
	// Initialize a TF computational API.
	@Bean(name = "tf")
	Ops getOps() {
		return Ops.create(context.getBean("graph", Graph.class));
	}

	// Initialize a parser for parsing the detection results.
	@Bean(name = "parser")
	DetectionResultParser getParser() throws IOException {
		return new DetectionResultParser("tf_inception/coco-labels-2017.txt");
	}

}
