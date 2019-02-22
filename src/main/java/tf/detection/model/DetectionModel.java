package tf.detection.model;

public class DetectionModel {
    private String filename;
    private String label;
    private float score;
    private float ymin;
    private float xmin;
    private float ymax;
    private float xmax;

    public DetectionModel(String filename, String label, float score, float ymin, float xmin, float ymax, float xmax) {
        this.filename = filename;
        this.label = label;
        this.score = score;
        this.ymin = ymin;
        this.xmin = xmin;
        this.ymax = ymax;
        this.xmax = xmax;
    }
}
