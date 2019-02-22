package tf.detection.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.framework.MetaGraphDef;
import org.tensorflow.framework.SignatureDef;
import org.tensorflow.framework.TensorInfo;
import org.tensorflow.types.UInt8;
import tf.detection.model.DetectionModel;
import tf.detection.repository.DetectionRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DetectionService {
    private final String[] detectionLabels;
    private final SavedModelBundle detectionModel;
    private final Session session;
    private final String testImagePath;

    @Autowired
    private DetectionRepository detectionRepository;

    public DetectionService(String[] detectionLabels, SavedModelBundle detectionModel, URL testImageURL) {
        this.detectionLabels = detectionLabels;
        this.detectionModel = detectionModel;
        this.session = detectionModel.session();
        this.testImagePath = testImageURL.getPath();

    }

    public String[] viewLabels() {
        return detectionLabels;
    }

    public List<String> viewSignature() throws Exception {
        return getModelSignature(detectionModel);
    }

    public void loadFakeDataIntoDB() {
        detectionRepository.save(new DetectionModel(
                "/Users/seansyue/WORKSPACE-Coding/SpringBootProjects/Custom/tf-mysql-detection/out/production/resources/tf_inception/test.jpg",
                "person",
                (float) 0.984342566432,
                (float) 0.3406806343433,
                (float) 0.34068063432423,
                (float) 0.3434234,
                (float) 0.342342352));
    }

    public List<String> simpleInference() throws Exception {

        List<Tensor<?>> outputs = getInferenceOutput(testImagePath, session);

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

            List<String> results = new ArrayList<>();
            results.add(testImagePath);
            results.addAll(parseDetectionResults(classes, detectionLabels, scores, boxes));

            return results;
        }
    }

    public List<DetectionModel> simpleInferenceWithModel() throws Exception {

        List<Tensor<?>> outputs = getInferenceOutput(testImagePath, session);

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

            List<DetectionModel> modelsList = new ArrayList<>();
            for (int i = 0; i < scores.length; ++i) {
                if (scores[i] < 0.5) {
                    continue;
                }
                DetectionModel model = new DetectionModel(
                        testImagePath, detectionLabels[(int) classes[i]], scores[i], boxes[i][0], boxes[i][1], boxes[i][2], boxes[i][3]);
                modelsList.add(model);
            }
            return modelsList;
        }
    }

    public String simpleInferenceDB() throws Exception {

        List<Tensor<?>> outputs = getInferenceOutput(testImagePath, session);

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

            for (int i = 0; i < scores.length; ++i) {
                if (scores[i] < 0.5) {
                    continue;
                }
                detectionRepository.save(new DetectionModel(
                        testImagePath,
                        detectionLabels[(int) classes[i]],
                        scores[i],
                        boxes[i][0],
                        boxes[i][1],
                        boxes[i][2],
                        boxes[i][3]));
            }

            return "Done!";
        }
    }

    private static List<String> parseDetectionResults(float[] classes, String[] detectionLabels, float[] scores, float[][] boxes) {
        // Collect all objects whose score is at least 0.5.
        List<String> results = new ArrayList<>();
        for (int i = 0; i < scores.length; ++i) {
            if (scores[i] < 0.5) {
                continue;
            }
            results.add(String.format("\tFound %-20s (score: %.4f) box: (%.4f, %.4f, %.4f, %.4f)\n",
                    detectionLabels[(int) classes[i]], scores[i], boxes[i][0], boxes[i][1], boxes[i][2], boxes[i][3]));
        }
        return results;
    }

    private static List<Tensor<?>> getInferenceOutput(String inferenceFilePath, Session session) throws Exception {
        List<Tensor<?>> outputs = null;
        try (Tensor<UInt8> input = makeImageTensor(inferenceFilePath)) {
            outputs =
                    session
                            .runner()
                            .feed("image_tensor", input)
                            .fetch("detection_scores")
                            .fetch("detection_classes")
                            .fetch("detection_boxes")
                            .run();
        }
        return outputs;
    }

    private static Tensor<UInt8> makeImageTensor(String filename) throws IOException {
        BufferedImage img = ImageIO.read(new File(filename));
        if (img.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            throw new IOException(
                    String.format(
                            "Expected 3-byte BGR encoding in BufferedImage, found %d (file: %s). This code could be made more robust",
                            img.getType(), filename));
        }
        byte[] data = ((DataBufferByte) img.getData().getDataBuffer()).getData();
        // ImageIO.read seems to produce BGR-encoded images, but the model expects RGB.
        bgr2rgb(data);
        final long BATCH_SIZE = 1;
        final long CHANNELS = 3;
        long[] shape = new long[] {BATCH_SIZE, img.getHeight(), img.getWidth(), CHANNELS};
        return Tensor.create(UInt8.class, shape, ByteBuffer.wrap(data));
    }

    private static void bgr2rgb(byte[] data) {
        for (int i = 0; i < data.length; i += 3) {
            byte tmp = data[i];
            data[i] = data[i + 2];
            data[i + 2] = tmp;
        }
    }

    private static List<String> getModelSignature(SavedModelBundle model) throws Exception {
        MetaGraphDef m = MetaGraphDef.parseFrom(model.metaGraphDef());
        SignatureDef sig = m.getSignatureDefOrThrow("serving_default");
        List<String> output = new ArrayList<>();

        int numInputs = sig.getInputsCount();
        int i = 1;
        output.add("MODEL SIGNATURE");
        output.add("Inputs:");
        for (Map.Entry<String, TensorInfo> entry : sig.getInputsMap().entrySet()) {
            TensorInfo t = entry.getValue();
            output.add(String.format(
                    "%d of %d: %-20s (Node name in graph: %-20s, type: %s)\n",
                    i++, numInputs, entry.getKey(), t.getName(), t.getDtype()));
        }

        int numOutputs = sig.getOutputsCount();
        i = 1;
        output.add("Outputs:");
        for (Map.Entry<String, TensorInfo> entry : sig.getOutputsMap().entrySet()) {
            TensorInfo t = entry.getValue();
            output.add(String.format(
                    "%d of %d: %-20s (Node name in graph: %-20s, type: %s)\n",
                    i++, numOutputs, entry.getKey(), t.getName(), t.getDtype()));
        }
        output.add("-----------------------------------------------");

        return output;
    }
}