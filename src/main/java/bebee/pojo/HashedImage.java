package bebee.pojo;

import java.io.InputStream;

public class HashedImage extends BeEmpResource{
    private String hash;

    
    public HashedImage(String filename) {
        this.filename = filename;
    }

    public HashedImage(String filename, String hash) {
        this.filename = filename;
        this.hash = hash;
    }

    public HashedImage(InputStream inputStream, String filename, String category) {
        this.inputStream = inputStream;
        this.filename = filename;
        this.category = category;
    }
    
    /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * @param hash the hash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

}
