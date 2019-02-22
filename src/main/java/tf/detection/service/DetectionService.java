package tf.detection.service;

import org.springframework.stereotype.Service;

@Service
public class DetectionService {
    private final String[] detectionLabels;

    public DetectionService(String[] detectionLabels) {
        this.detectionLabels = detectionLabels;
    }

    public String[] viewAllLabels() {
        return detectionLabels;
    }
}