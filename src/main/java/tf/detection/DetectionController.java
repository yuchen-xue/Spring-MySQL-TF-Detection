package tf.detection;

import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import tf.detection.dao.DetectedObject;
import tf.detection.detection.DetectionService;
import tf.detection.storage.StorageFileNotFoundException;
import tf.detection.storage.StorageService;

@Controller
public class DetectionController {

	private final StorageService storageService;

    private final DetectionService detectionService;

    public DetectionController(DetectionService detectionService, StorageService storageService) {
        this.detectionService = detectionService;
        this.storageService = storageService;
    }

	@GetMapping("/")
	public String listResultFiles(Model model) {

		model.addAttribute("files", storageService.loadAllResult().map(
				path -> MvcUriComponentsBuilder.fromMethodName(DetectionController.class,
						"serveFile", path.getFileName().toString()).build().toUri().toString())
				.collect(Collectors.toList()));

		return "uploadForm";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadResultResource(filename);

		if (file == null)
			return ResponseEntity.notFound().build();

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

		storageService.store(file);
		redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
        
        // Inference the uploaded image and save the result
        String uploadImg = storageService.getLatestUploadFile().toString();
        String resultImg = storageService.getLatestResultFile().toString();
        detectionService.inference(uploadImg, resultImg);

		return "redirect:/";
	}

    @GetMapping(value = "/result")
	@ResponseBody
    public Iterable<DetectedObject> getAllResults() {
        return detectionService.getAllResults();
    }

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}
}