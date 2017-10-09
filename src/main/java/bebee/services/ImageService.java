package bebee.services;

import bebee.beans.HashedImageBean;
import bebee.pojo.HashedImage;


/**
 * Created by Rivas on 31/3/17.
 */
public interface ImageService {
    HashedImage compareImage(HashedImageBean hashedImageBean) throws Exception;
    HashedImage insertImage(HashedImageBean hashedImageBean) throws Exception;
    void deleteImage(HashedImage hashedImage) throws Exception;
}
