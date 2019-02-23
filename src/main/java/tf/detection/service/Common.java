package tf.detection.service;

import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

class Common {

    static List<Tensor<?>> getInferenceTensor(String inferenceFilePath, Session session) throws Exception {
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

}
