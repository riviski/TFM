package bebee.pojo;

import java.io.InputStream;

/**
 * Created by Rivas on 5/5/17.
 */
public class BeEmpResource implements Comparable<BeEmpResource>{
    protected String filename;
    protected String category;
    protected InputStream inputStream;
    protected double similarity;

    public BeEmpResource(String filename) {
        this.filename = filename;
    }

    public BeEmpResource() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public int compareTo(BeEmpResource other) {
        return Double.compare(this.similarity, other.similarity);
    }
}
