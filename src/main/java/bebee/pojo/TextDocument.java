package bebee.pojo;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/**
 * Created by Rivas on 5/5/17.
 */
public class TextDocument extends BeEmpResource{
    private Date date;
    private Map<String, Double> tagsMap;

    public TextDocument(String filename, String category, Date date, InputStream inputStream) {
        this.filename = filename;
        this.category = category;
        this.date = date;
        this.inputStream = inputStream;
    }

    public TextDocument(String filename, Map<String, Double> tagsMap) {
        this.filename = filename;
        this.tagsMap = tagsMap;
    }

    public TextDocument(String filename) {
        this.filename = filename;
    }

    public TextDocument(String filename, double similarity) {
        this.filename = filename;
        this.similarity = similarity;
    }

    public TextDocument(String fileName, Date date, String category, Map<String, Double> tagsMap) {
        this.filename = fileName;
        this.category = category;
        this.date = date;
        this.tagsMap = tagsMap;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Map<String, Double> getTagsMap() {
        return tagsMap;
    }

    public void setTagsMap(Map<String, Double> tagsMap) {
        this.tagsMap = tagsMap;
    }
}
