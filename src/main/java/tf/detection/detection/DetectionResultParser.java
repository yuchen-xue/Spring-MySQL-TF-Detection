package tf.detection.detection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Table;

public final class DetectionResultParser {

    private String[] labelArray;
    private Map<Integer, Map<String, Float>> rowMap;

    public DetectionResultParser(String labelFile) throws IOException {
        loadExternalLabels(labelFile);
    }

    private void loadExternalLabels(String labelFile) throws IOException {
        /**
         * Load COCO dataset labels from an extertal text file.
         */
        try {
            String strFilePath = ClassLoader.getSystemResource(labelFile).getPath();
            Path filePath = Paths.get(strFilePath);
            List<String> lines = Files.lines(filePath).collect(Collectors.toList());
            this.labelArray = lines.toArray(new String[0]);
        } catch (IOException e) {
            throw new IOException("Failed to load labels from file: " + labelFile, e);
        }
    }

    public void load(Table<Integer, String, Float> table) {
        /*
         * Load the table of detection results and inteprate it as a mapping of rows.
         */
        this.rowMap = table.rowMap();
    }

    public Set<Integer> getKeySetPerRow() {
        /*
         * Get the set of keys for each row in the table.
         */
        return this.rowMap.keySet();
    }

    public String getLabelByRow(int row) {
        /*
         * Get the detection class of the row.
         */
        return this.labelArray[Math.round(this.rowMap.get(row).get("detection_class")) - 1];
    }

    public float getScoreByRow(int row) {
        /*
         * Get the detection score of the row.
         */
        return this.rowMap.get(row).get("detection_score");
    }

    public float getYminByRow(int row) {
        /*
         * Get the relative y-coordinate of the bottom-right corner of the bounding box.
         */
        return this.rowMap.get(row).get("ymin");
    }

    public float getXminByRow(int row) {
        /*
         * Get the relative x-coordinate of the bottom-right corner of the bounding box.
         */
        return this.rowMap.get(row).get("xmin");
    }

    public float getYmaxByRow(int row) {
        /*
         * Get the relative y-coordinate of the top-left corner of the bounding box.
         */
        return this.rowMap.get(row).get("ymax");
    }

    public float getXmaxByRow(int row) {
        return this.rowMap.get(row).get("xmax");
    }
}
