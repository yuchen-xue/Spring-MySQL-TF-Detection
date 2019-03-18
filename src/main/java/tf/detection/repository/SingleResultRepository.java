package tf.detection.repository;

import org.springframework.data.repository.CrudRepository;
import tf.detection.dao.SingleResult;

public interface SingleResultRepository extends CrudRepository<SingleResult, Integer> {
}
