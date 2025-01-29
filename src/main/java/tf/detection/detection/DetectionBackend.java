package tf.detection.detection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.tensorflow.Graph;
import org.tensorflow.Operand;
import org.tensorflow.Result;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.op.Ops;
import org.tensorflow.op.core.Constant;
import org.tensorflow.op.core.Placeholder;
import org.tensorflow.op.core.Reshape;
import org.tensorflow.op.image.DecodeJpeg;
import org.tensorflow.op.image.EncodeJpeg;
import org.tensorflow.op.io.ReadFile;
import org.tensorflow.op.io.WriteFile;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TString;
import org.tensorflow.types.TUint8;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;


/**
 * Loads an image using ReadFile and DecodeJpeg and then uses the saved model
 * to detect objects with a detection score greater than 0.3
 * Uses the DrawBounding boxes
 */
public final class DetectionBackend {

    private static DecodeJpeg getDecodedImage(Ops tf, String imagePath) {
        Constant<TString> fileName = tf.constant(imagePath);
        ReadFile readFile = tf.io.readFile(fileName);
        DecodeJpeg.Options options = DecodeJpeg.channels(3L);
        return tf.image.decodeJpeg(readFile.contents(), options);
    }

    private static Table<Integer, String, Float> processDetectionResult(Session.Runner runner, Ops tf,
            Result outputTensorMap, Shape imageShape, TUint8 reshapeTensor, String outputImagePath) {

        TFloat32 numDetections = (TFloat32) outputTensorMap.get("num_detections").get();
        int numDetects = (int) numDetections.getFloat(0);

        // Create a table to store the results
        Table<Integer, String, Float> resultTable = HashBasedTable.create();

        if (numDetects > 0) {
            TFloat32 detectionBoxes = (TFloat32) outputTensorMap.get("detection_boxes").get();
            TFloat32 detectionScores = (TFloat32) outputTensorMap.get("detection_scores").get();
            TFloat32 detectionClasses = (TFloat32) outputTensorMap.get("detection_classes").get();
            ArrayList<FloatNdArray> boxArray = new ArrayList<>();

            // TODO tf.image.combinedNonMaxSuppression
            for (int n = 0; n < numDetects; n++) {
                // put probability and position in outputMap
                float detectionScore = detectionScores.getFloat(0, n);
                float detectionClass = detectionClasses.getFloat(0, n);

                // only include those classes with detection score greater than 0.3f
                if (detectionScore > 0.3f) {
                    FloatNdArray detectionBox = detectionBoxes.get(0, n);
                    boxArray.add(detectionBox);

                    // Collect detection results
                    resultTable.put(n, "detection_class", detectionClass);
                    resultTable.put(n, "detection_score", detectionScore);
                    resultTable.put(n, "ymin", detectionBox.getFloat(0));
                    resultTable.put(n, "xmin", detectionBox.getFloat(1));
                    resultTable.put(n, "ymax", detectionBox.getFloat(2));
                    resultTable.put(n, "xmax", detectionBox.getFloat(3));
                }
            }

            // 2-D. A list of RGBA colors to cycle through for the boxes.
            Operand<TFloat32> colors = tf.constant(new float[][] {
                    { 0.9f, 0.3f, 0.3f, 0.0f },
                    { 0.3f, 0.3f, 0.9f, 0.0f },
                    { 0.3f, 0.9f, 0.3f, 0.0f }
            });

            Shape boxesShape = Shape.of(1, boxArray.size(), 4);
            int boxCount = 0;
            // 3-D with shape `[batch, num_bounding_boxes, 4]` containing bounding boxes
            try (TFloat32 boxes = TFloat32.tensorOf(boxesShape)) {
                // batch size of 1
                boxes.setFloat(1, 0, 0, 0);
                for (FloatNdArray floatNdArray : boxArray) {
                    boxes.set(floatNdArray, 0, boxCount);
                    boxCount++;
                }

                // Placeholders for boxes and path to outputimage
                Placeholder<TFloat32> boxesPlaceHolder = tf.placeholder(TFloat32.class, Placeholder.shape(boxesShape));
                Placeholder<TString> outImagePathPlaceholder = tf.placeholder(TString.class);
                // Create JPEG from the Tensor with quality of 100%
                EncodeJpeg.Options jpgOptions = EncodeJpeg.quality(100L);
                // convert the 4D input image to normalised 0.0f - 1.0f
                // Draw bounding boxes using boxes tensor and list of colors
                // multiply by 255 then reshape and recast to TUint8 3D tensor
                WriteFile writeFile = tf.io.writeFile(outImagePathPlaceholder,
                        tf.image.encodeJpeg(
                                tf.dtypes.cast(tf.reshape(
                                        tf.math.mul(
                                                tf.image.drawBoundingBoxes(tf.math.div(
                                                        tf.dtypes.cast(tf.constant(reshapeTensor),
                                                                TFloat32.class),
                                                        tf.constant(255.0f)),
                                                        boxesPlaceHolder, colors),
                                                tf.constant(255.0f)),
                                        tf.array(
                                                imageShape.asArray()[0],
                                                imageShape.asArray()[1],
                                                imageShape.asArray()[2])),
                                        TUint8.class),
                                jpgOptions));
                // output the JPEG to file
                runner.feed(outImagePathPlaceholder, TString.scalarOf(outputImagePath))
                        .feed(boxesPlaceHolder, boxes)
                        .addTarget(writeFile).run();
            }
        }
        return resultTable;
    }

    public static Table<Integer, String, Float> runDetectionTask(SavedModelBundle model, Graph g, Ops tf,
            String imagePath, String outputImagePath) {

        try (Session s = new Session(g)) {
            DecodeJpeg decodeImage = getDecodedImage(tf, imagePath);

            Shape imageShape;
            try (var shapeResult = s.runner().fetch(decodeImage).run()) {
                imageShape = shapeResult.get(0).shape();
            }

            // reshape the tensor to 4D in order to feed it into the model
            Reshape<TUint8> reshape = tf.reshape(decodeImage,
                    tf.array(1,
                            imageShape.asArray()[0],
                            imageShape.asArray()[1],
                            imageShape.asArray()[2]));

            try (var reshapeResult = s.runner().fetch(reshape).run()) {
                TUint8 reshapeTensor = (TUint8) reshapeResult.get(0);
                Map<String, Tensor> feedDict = new HashMap<>();
                // The given SavedModel SignatureDef input
                feedDict.put("input_tensor", reshapeTensor);
                // The given SavedModel MetaGraphDef key
                // detection_classes, detectionBoxes etc. are model output names
                try (Result outputTensorMap = model.function("serving_default").call(feedDict)) {
                    return processDetectionResult(s.runner(), tf, outputTensorMap, imageShape, reshapeTensor,
                            outputImagePath);
                }
            }
        }
    }
}
