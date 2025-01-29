package tf.detection.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import tf.detection.dao.DetectedObject;
import tf.detection.service.DetectionService;

@RestController
public class DetectionController {

    private DetectionService detectionService;

    public DetectionController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @PostMapping(value = "/db/upload")
    public String runUploadInferenceDB(@RequestBody String uploadedImagePath) throws Exception{
        return detectionService.uploadInferenceDB(uploadedImagePath);
    }

    @GetMapping(value = "/db/all")
    public Iterable<DetectedObject> getAllResults() {
        return detectionService.getAllResults();
    }
}