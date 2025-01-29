package tf.detection.repository;

import org.springframework.data.repository.CrudRepository;
import tf.detection.dao.DetectedObject;

public interface DetectedObjectRepository extends CrudRepository<DetectedObject, Integer> {
}
