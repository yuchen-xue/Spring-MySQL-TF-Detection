package tf.detection.repository;

import org.springframework.data.repository.CrudRepository;
import tf.detection.model.DetectionModel;


public interface DetectionRepository extends CrudRepository<DetectionModel, String> {
}
