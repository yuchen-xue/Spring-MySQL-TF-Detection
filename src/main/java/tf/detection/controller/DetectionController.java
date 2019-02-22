package tf.detection.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tf.detection.service.DetectionService;

@RestController
public class DetectionController {

    private DetectionService detectionService;

    public DetectionController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @GetMapping(value = "/view_labels")
    public String[] viewAllLabels() {
        return detectionService.viewAllLabels();
    }
}