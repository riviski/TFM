package bebee.dao;

import bebee.pojo.HashedImage;
import bebee.pojo.TextDocument;
import com.mongodb.MongoException;

import java.util.List;

/**
 * Created by Rivas on 4/4/17.
 */
public interface InterfaceDAO {
    void saveDocument(TextDocument textDocument) throws Exception;
    void deleteDocument(TextDocument textDocument) throws Exception;
    void saveImage(HashedImage hashedImage) throws Exception;
    void deleteImage(HashedImage hashedImage) throws MongoException;
    HashedImage findIdenticalImage(HashedImage hashedImage) throws Exception;
    HashedImage findSimilarImage(HashedImage hashedImage) throws Exception;
    List<TextDocument> findSimilarDocument(TextDocument textDocument) throws Exception;
}
