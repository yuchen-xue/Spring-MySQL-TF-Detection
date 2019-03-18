package tf.detection.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import tf.detection.dao.Result;
import tf.detection.dao.SingleResult;
import tf.detection.repository.DetectionRepository;
import tf.detection.repository.SingleResultRepository;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static tf.detection.service.Common.getInferenceTensor;

@Service
public class DBService {
    private final String[] detectionLabels;
    private final Session session;
    private final String testImagePath;

    @Autowired
    private DetectionRepository detectionRepository;

    @Autowired
    private SingleResultRepository singleResultRepository;

    public DBService(String[] detectionLabels, SavedModelBundle detectionModel, URL testImageURL) {
        this.detectionLabels = detectionLabels;
        this.session = detectionModel.session();
        this.testImagePath = testImageURL.getPath();
    }

    public String simpleInferenceDB() throws Exception {
        inferenceDB(testImagePath);
        return "Done!";
    }

    public String uploadInferenceDB(String uploadedImagePath) throws Exception {
        inferenceDB(uploadedImagePath);
        return "Done!";
    }

    public Iterable<Result> getAllResults() {
        return detectionRepository.findAll();
    }

    private void inferenceDB(String imagePath) throws Exception {

        List<Tensor<?>> outputs = getInferenceTensor(imagePath, session);

        try (Tensor<Float> scoresT = outputs.get(0).expect(Float.class);
             Tensor<Float> classesT = outputs.get(1).expect(Float.class);
             Tensor<Float> boxesT = outputs.get(2).expect(Float.class)) {
            // All these tensors have:
            // - 1 as the first dimension
            // - maxObjects as the second dimension
            // While boxesT will have 4 as the third dimension (2 sets of (x, y) coordinates).
            // This can be verified by looking at scoresT.shape() etc.
            int maxObjects = (int) scoresT.shape()[1];
            float[] scores = scoresT.copyTo(new float[1][maxObjects])[0];
            float[] classes = classesT.copyTo(new float[1][maxObjects])[0];
            float[][] boxes = boxesT.copyTo(new float[1][maxObjects][4])[0];

            List<SingleResult> singleResultList = new ArrayList<>();
            Result result = new Result(imagePath, singleResultList);
            detectionRepository.save(result);

            for (int i = 0; i < scores.length; ++i) {
                if (scores[i] < 0.5) {
                    continue;
                }

                SingleResult singleResult = new SingleResult(
                        detectionLabels[(int) classes[i]],
                        scores[i],
                        boxes[i][0],
                        boxes[i][1],
                        boxes[i][2],
                        boxes[i][3],
                        result);
                singleResultRepository.save(singleResult);
            }
        }
    }
}