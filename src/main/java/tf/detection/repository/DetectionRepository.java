package tf.detection.repository;

import org.springframework.data.repository.CrudRepository;
import tf.detection.dao.ResultBundle;


public interface DetectionRepository extends CrudRepository<ResultBundle, String> {
}
