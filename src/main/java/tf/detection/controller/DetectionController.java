package tf.detection.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tf.detection.model.DetectionModel;
import tf.detection.service.DetectionService;

import java.util.List;

@RestController
public class DetectionController {

    private DetectionService detectionService;

    public DetectionController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @GetMapping(value = "/view_labels")
    public String[] viewModelLabels() {
        return detectionService.viewLabels();
    }

    @GetMapping(value = "/view_signature")
    public List<String> viewModelSignature() throws Exception {
        return detectionService.viewSignature();
    }

    @GetMapping(value = "/simple_inference")
    public List<String> runSimpleInferece() throws Exception {
        return detectionService.simpleInference();
    }

    @GetMapping(value = "/model_inference")
    public List<DetectionModel> runSimpleInferenceWithModel() throws Exception {
        return detectionService.simpleInferenceWithModel();
    }
}