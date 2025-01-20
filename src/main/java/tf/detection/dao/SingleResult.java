package tf.detection.dao;

import jakarta.persistence.*;

@Entity
@Table(name = "results")
public class SingleResult {

    @Id
    @GeneratedValue
    private Integer id;
    private String label;
    private float score;
    private float ymin;
    private float xmin;
    private float ymax;
    private float xmax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_index")
    private Result result;

    public SingleResult() {
    }

    public SingleResult(String label, float score, float ymin, float xmin, float ymax, float xmax, Result result) {
        this.label = label;
        this.score = score;
        this.ymin = ymin;
        this.xmin = xmin;
        this.ymax = ymax;
        this.xmax = xmax;
        this.result = result;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getYmin() {
        return ymin;
    }

    public void setYmin(float ymin) {
        this.ymin = ymin;
    }

    public float getXmin() {
        return xmin;
    }

    public void setXmin(float xmin) {
        this.xmin = xmin;
    }

    public float getYmax() {
        return ymax;
    }

    public void setYmax(float ymax) {
        this.ymax = ymax;
    }

    public float getXmax() {
        return xmax;
    }

    public void setXmax(float xmax) {
        this.xmax = xmax;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
