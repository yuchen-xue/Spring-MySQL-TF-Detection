package tf.detection.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import org.tensorflow.Graph;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.op.Ops;

import com.google.common.collect.Table;

import tf.detection.dao.DetectedObject;
import tf.detection.detection.DetectionBackend;
import tf.detection.repository.DetectedObjectRepository;
import tf.detection.detection.DetectionResultParser;

@Service
public class DetectionService {

    @Autowired
    private DetectedObjectRepository detectedObjectRepository;

	@Autowired
    private ApplicationContext context;

    public DetectionService() {}

    public String uploadInferenceDB(String uploadedImagePath) throws Exception {
		// Perform obejct detection on the the uploaded image
		Table<Integer, String, Float> resultTable = DetectionBackend.runDetectionTask(
			context.getBean("model", SavedModelBundle.class), 
			context.getBean("graph", Graph.class), 
			context.getBean("tf", Ops.class), 
			uploadedImagePath, 
			"result.jpg");

		// Create a parser for parsing the detection results
		DetectionResultParser parser = context.getBean("parser", DetectionResultParser.class);
	
		// Initialize the parser with the table
		parser.load(resultTable);

		// Iterate the row mapping print the data of each row
		for (Integer row : parser.getKeySetPerRow()) {
			DetectedObject detectedObject = new DetectedObject (
				parser.getLabelByRow(row),
				parser.getScoreByRow(row),
				parser.getYminByRow(row),
				parser.getXminByRow(row),
				parser.getYmaxByRow(row),
				parser.getXmaxByRow(row)
			);
			detectedObjectRepository.save(detectedObject);
		}
        return "Done!";
    }

    public Iterable<DetectedObject> getAllResults() {
        return detectedObjectRepository.findAll();
    }
}