package tf.detection.service;

import org.springframework.stereotype.Service;
import org.tensorflow.SavedModelBundle;

@Service
public class ViewService {
    private final String[] detectionLabels;
    private final SavedModelBundle detectionModel;

    public ViewService(String[] detectionLabels, SavedModelBundle detectionModel) {
        this.detectionLabels = detectionLabels;
        this.detectionModel = detectionModel;
    }

    public String[] viewLabels() {
        return detectionLabels;
    }
}