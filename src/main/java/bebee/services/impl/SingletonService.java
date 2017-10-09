package bebee.services.impl;

import bebee.dao.impl.MongoDAO;

import java.net.UnknownHostException;

/**
 * Created by Rivas on 8/5/17.
 */
public class SingletonService {
    private MongoDAO mongoDAO = null;

    public MongoDAO getMongoDAO() throws UnknownHostException {
        if(mongoDAO == null){
            mongoDAO = MongoDAO.getInstance();
        }
        return mongoDAO;
    }
}
