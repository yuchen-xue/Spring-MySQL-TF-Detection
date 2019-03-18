package tf.detection.repository;

import org.springframework.data.repository.CrudRepository;
import tf.detection.dao.Result;


public interface DetectionRepository extends CrudRepository<Result, Integer> {
}
