package tf.detection.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tf.detection.dao.ResultBundle;
import tf.detection.service.DBService;
import tf.detection.service.TestService;
import tf.detection.service.ViewService;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

@RestController
public class DetectionController {

    private ViewService viewService;
    private DBService dbService;
    private TestService testService;

    public DetectionController(ViewService viewService, DBService dbService, TestService testService) {
        this.viewService = viewService;
        this.dbService = dbService;
        this.testService = testService;
    }

    @GetMapping(value = "/view/labels")
    public String[] viewModelLabels() {
        return viewService.viewLabels();
    }

    @GetMapping(value = "/view/model_signature")
    public void viewModelSignature(HttpServletResponse res) throws Exception {
        PrintWriter out = res.getWriter();
        out.println(viewService.viewSignature());
        out.close();
    }

    @GetMapping(value = "/test/simple_inference")
    public void runSimpleInference(HttpServletResponse res) throws Exception {
        PrintWriter out = res.getWriter();
        out.println(testService.simpleInference());
        out.close();
    }

    @GetMapping(value = "/test/dao_inference")
    public List<ResultBundle> runSimpleInferenceWithModel() throws Exception {
        return testService.simpleInferenceDAO();
    }

    @GetMapping(value = "/test/load_fake")
    public String runLoadFakeDataIntoDB() {
        return testService.loadFakeDataIntoDB();
    }

    @GetMapping(value = "/db/sample_inference")
    public String runSimpleInferenceDB() throws Exception{
        return dbService.simpleInferenceDB();
    }

    @GetMapping(value = "/db/all")
    public Iterable<ResultBundle> getAllResults() {
        return dbService.getAllResults();
    }
}