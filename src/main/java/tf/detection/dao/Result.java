package tf.detection.dao;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "inference_files")
public class Result {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "file_name", unique = true, nullable = false)
    private String filename;

    @OneToMany(mappedBy = "result")
    private List<SingleResult> singleResultList;

    public Result() {
    }

    public Result(String filename, List<SingleResult> singleResultList) {
        this.filename = filename;
        this.singleResultList = singleResultList;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<SingleResult> getSingleResultList() {
        return singleResultList;
    }

    public void setSingleResultList(List<SingleResult> singleResultList) {
        this.singleResultList = singleResultList;
    }
}
