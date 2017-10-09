package bebee.dao.impl;

/**
 * Created by Rivas on 31/3/17.
 */

import bebee.pojo.BeEmpResource;
import bebee.pojo.TextDocument;
import com.mongodb.*;
import bebee.dao.InterfaceDAO;
import bebee.pojo.HashedImage;
import bebee.utils.DBConstants;
import org.apache.log4j.Logger;

import java.net.UnknownHostException;
import java.util.*;

public final class MongoDAO implements InterfaceDAO{
    private static Logger logger = Logger.getLogger(MongoDAO.class);

    private static MongoDAO mongoDAO;
    private static MongoClient mongoClient;
    private static DB db;

    private MongoDAO(){
    }

    public static MongoDAO getInstance() throws UnknownHostException {
        if(mongoDAO == null){
            mongoDAO = new MongoDAO();
            mongoDAO.getDB();
        }
        return mongoDAO;
    }

    private void getDB() throws UnknownHostException {
        if(mongoClient == null){
            try {
                mongoClient = new MongoClient(DBConstants.DBHOST , DBConstants.DBPORT);
            } catch (UnknownHostException e) {
                logger.error("Error al iniciar la base de datos");
                throw e;
            }
        }
        if(db == null) {
            db = mongoClient.getDB(DBConstants.DBNAME);
        }
        if(!db.isAuthenticated()){
            boolean auth = db.authenticate(DBConstants.DBUNAME, DBConstants.DBPASS.toCharArray());
        }
    }

    @Override
    public void saveDocument(TextDocument textDocument) throws Exception {
        DBCollection dbCollection = db.getCollection(DBConstants.TEXTS_COLLECTION);
        BasicDBObject document = new BasicDBObject();

        document.put("filename", textDocument.getFilename());
        document.put("date", textDocument.getDate());
        document.put("tagsMap", textDocument.getTagsMap());
        String category = textDocument.getCategory();
        if (null != category && category.length() > 0) {
            document.put("category", category);
        }else{
            document.put("category", "");
        }

        dbCollection.insert(document);
    }

    @Override
    public void deleteDocument(TextDocument textDocument) throws Exception {
        DBCollection dbCollection = db.getCollection(DBConstants.TEXTS_COLLECTION);
        delete(textDocument, dbCollection);
    }

    @Override
    public void saveImage(HashedImage hashedImage){
        DBCollection dbCollection = db.getCollection(DBConstants.HASHES_COLLECTION);
        BasicDBObject document = new BasicDBObject();

        document.put("filename", hashedImage.getFilename());
        document.put("hash", hashedImage.getHash());
        String category = hashedImage.getCategory();
        if (null != category && category.length() > 0) {
            document.put("category", category);
        }

        dbCollection.insert(document);
    }

    @Override
    public void deleteImage(HashedImage hashedImage)throws MongoException{
        DBCollection dbCollection = db.getCollection(DBConstants.HASHES_COLLECTION);
        delete(hashedImage, dbCollection);
    }

    @Override
    public HashedImage findIdenticalImage(HashedImage hashedImage){
        DBCollection dbCollection = db.getCollection(DBConstants.HASHES_COLLECTION);
        HashedImage hi = null;

        BasicDBObject query = new BasicDBObject();
        query.put("hash", hashedImage.getHash());
        if(null != hashedImage.getCategory() && hashedImage.getCategory().length() > 0){
            query.put("category", hashedImage.getCategory());
        }

        DBCursor cursor = dbCollection.find(query);
        while (cursor.hasNext()) {
            DBObject result = cursor.next();
            hi = new HashedImage(result.get("filename").toString(), result.get("hash").toString());
            hi.setSimilarity(100);
        }
        return hi;
    }

    @Override
    public HashedImage findSimilarImage(HashedImage hashedImage){
        DBCollection dbCollection = db.getCollection(DBConstants.HASHES_COLLECTION);
        HashedImage hi = null;
        String category = hashedImage.getCategory();
        String originalHash = hashedImage.getHash();
        double minDistance = 101.0;

        BasicDBObject query = new BasicDBObject();
        DBCursor cursor;
        if(category.length() > 0){ //find only in same category
            query.put("category", category);
            cursor = dbCollection.find(query);
        }else{ //find in all the image dataset
            cursor = dbCollection.find();
        }

        while (cursor.hasNext()) {
            DBObject result = cursor.next();
            String hash = result.get("hash").toString();

            int distance = distance(originalHash, hash, minDistance);
            if (distance < minDistance) {
                double difference = (distance * 100) / hash.length();
                minDistance = distance;
                hi = new HashedImage(result.get("filename").toString(), hash);
                hi.setSimilarity(100-difference);
            }
        }

        return hi;
    }

    @Override
    public List<TextDocument> findSimilarDocument(TextDocument textDocument){
        DBCollection dbCollection = db.getCollection(DBConstants.TEXTS_COLLECTION);
        List<TextDocument> documentsResult = new ArrayList<>();
        String category = textDocument.getCategory();
        Map<String, Double> tagsMap = textDocument.getTagsMap();

        // Lista con las palabras que se repiten más de un 2% como umbral en el documento.
        List<String> tags = getMoreFrecuentTags(tagsMap, 2.0, 0);
        if(tags.isEmpty()) {
            tags = getMoreFrecuentTags(tagsMap, 1.0, null);
        }

        BasicDBObject query = new BasicDBObject();
        DBCursor cursor;

        // Se realiza una búsqueda en BD filtrando por categoría y por las palabras que se repiten más de un umbral
        // previamente recuperadas. Se filtran los documentos en los que que alguna de éstas palabras
        // aparezca al menos un 1% en el documento.
        if(!category.isEmpty() && !tags.isEmpty()) { //find only in same category
            List<BasicDBObject> and = new ArrayList<>();
            and.add(new BasicDBObject("category", category));
            query.put("$and", and);

            List<BasicDBObject> or = new ArrayList<>();
            for(String tag : tags){
                or.add(new BasicDBObject("tagsMap."+tag, new BasicDBObject("$gt",1)));
            }
            query.put("$or", or);
        }else if(!category.isEmpty()){
            query.put("category", category);
        }else if(!tags.isEmpty()){
            List<BasicDBObject> or = new ArrayList<>();
            for(String tag : tags){
                or.add(new BasicDBObject("tagsMap."+tag, new BasicDBObject("$gt",1)));
            }
            query.put("$or", or);
        }

        // El objeto cursor contiene documentos filtrados por BD.
        List<TextDocument> documents = new ArrayList<>();
        cursor = dbCollection.find(query);
        while(cursor.hasNext()){
            DBObject result = cursor.next();
            Map<String, Double> tempTags = (Map<String, Double>) result.get("tagsMap");
            Date date = (Date) result.get("date");
            String fileName = (String) result.get("filename");
            String catego = (String) result.get("category");

            TextDocument doc = new TextDocument(fileName, date, catego, tempTags);
            documents.add(doc);
        }

        List<TextDocument> allDocuments = new ArrayList<>();
        cursor = null;
        cursor = dbCollection.find();
        while(cursor.hasNext()){
            DBObject result = cursor.next();
            Map<String, Double> tempTags = (Map<String, Double>) result.get("tagsMap");
            Date date = (Date) result.get("date");
            String fileName = (String) result.get("filename");
            String catego = (String) result.get("category");

            TextDocument doc = new TextDocument(fileName, date, catego, tempTags);
            allDocuments.add(doc);
        }

        Map<String, Double> tfIdf = getTFIDF(tagsMap, allDocuments);

        //Se itera en cada uno de los documentos recuperados por BD.
        for (TextDocument doc : documents){
            int i=0;
            int similarity=0;
            int extraSimilarity=0;
            int total=0;

            // Lista con las 5 palabras más relevantes del documento
            List<String> relevantTempTags = getMoreFrecuentTags(doc.getTagsMap(), null, 5);

            // Se evalúan las palabras más repetidas en el documento a comparar frente al documento
            // recuperado para calcular la similitud.
            for(String key : tfIdf.keySet()){
                double value = tfIdf.get(key);

                // Si la palabra existe en el documento comparado, se va sumando un valor de similitud
                // dependiento del cuartil en que se encuentre dentro de la lista ordenada de valores.
                if(value>0){
                    if(i<5){
                        total += 12;
                    }else if(i<10){
                        total += 5;
                    }else if(i<15){
                        total += 2;
                    }else{
                        total += 1;
                    }
                    if(doc.getTagsMap().containsKey(key)){
                        if(i<5){
                            similarity += 12;
                        }else if(i<10){
                            similarity += 5;
                        }else if(i<15){
                            similarity += 2;
                        }else{
                            similarity += 1;
                        }
                    }
                }

                // Se evalúa positivamente extra si la palabra es de las más relevantes entre ambos documentos.
                if (relevantTempTags.contains(key)) {
                    extraSimilarity += 50;
                }
                i++;
            }

            int tempSimilarity=0;
            int tempExtraSimilarity=0;
            int totalSimilarity=0;
            if(similarity!=0) {
                tempSimilarity = (similarity * 100) / total;
            }
            if(extraSimilarity!=0) {
                tempExtraSimilarity = (extraSimilarity * 100) / (relevantTempTags.size()*50);
            }

           if((tempSimilarity>0 && tempSimilarity<5 && tempExtraSimilarity>50) || (tempSimilarity == 0 && tempExtraSimilarity >=80)){
                totalSimilarity = (tempSimilarity*25+75*tempExtraSimilarity)/100;
            }else if(tempSimilarity>0 && tempSimilarity<5 && tempExtraSimilarity>40){
                totalSimilarity = (tempSimilarity*20+80*tempExtraSimilarity)/100;
            }else if(tempSimilarity >=5 && tempSimilarity<60 && tempExtraSimilarity > 49){
                totalSimilarity = (tempSimilarity*30+70*tempExtraSimilarity)/100;
            }else if(tempSimilarity >=5 && tempSimilarity<60 && tempExtraSimilarity <= 49){
                totalSimilarity = (tempSimilarity*50+50*tempExtraSimilarity)/100;
            }else if(tempSimilarity>59){
                totalSimilarity = (tempSimilarity*70+30*tempExtraSimilarity)/100;
            }
            if (totalSimilarity > 40) {
                doc.setSimilarity(totalSimilarity);
                documentsResult.add(doc);
            }

        }
        Collections.sort(documentsResult);
        Collections.reverse(documentsResult);
        return documentsResult;
    }

    private Map<String, Double> getTFIDF(Map<String, Double> tagsMap, List<TextDocument> documents) {
        Map<String, Double> tfIdf = new HashMap<>(tagsMap.size());
        for(String tag : tagsMap.keySet()) {
            int docsContaining = 0;
            for (TextDocument doc : documents) {
                if(doc.getTagsMap().containsKey(tag)){
                    docsContaining++;
                }
            }

            double tf = tagsMap.get(tag);
            double idf = Math.log(documents.size()/(1+docsContaining));
            double total = tf*idf;
            tfIdf.put(tag, total);
        }

        Map<String, Double> result = new HashMap<>();
        result = sortMap(tfIdf);

        return result;
    }

    private List<String> getMoreFrecuentTags(Map<String, Double> tagsMap, Double threshold, Integer number){
        List<String> frecuentTags = new ArrayList<>();
        for(Map.Entry<String, Double> entry : tagsMap.entrySet()) {
            if(null != threshold && entry.getValue() > threshold) {
                frecuentTags.add(entry.getKey());
            }else if(null != number && frecuentTags.size() < number){
                frecuentTags.add(entry.getKey());
            }
        }
        return frecuentTags;
    }

    private void delete(BeEmpResource beEmpResource, DBCollection dbCollection) {
        BasicDBObject query = new BasicDBObject();
        DBCursor cursor;

        query.put("filename", beEmpResource.getFilename());
        cursor = dbCollection.find(query);
        if(!cursor.hasNext()) {
            throw new MongoException("Object not found in collection: "+ dbCollection.getName());
        }
        while (cursor.hasNext()) {
            DBObject result = cursor.next();
            dbCollection.remove(result);
        }
    }

    /**
     * Calcula la Hamming distance entre dos hashes pasados como entrada con la
     * restriccion de que cuando supera cierto umbral, detiene la comprobacion.
     *
     * @param s1 primer hash
     * @param s2 segundo hash
     * @param currentMinDistance distancia minima actual, que es el valor en el
     * que se debe detener la comparacion
     * @return diferencia entre s1 y s2 hasta el currentMinDistance
     */
    private int distance(String s1, String s2, double currentMinDistance) {
        int counter = 0;
        for (int k = 0; k < s1.length(); k++) {
            if (s1.charAt(k) != s2.charAt(k)) {
                counter++;
            }
            if (counter > currentMinDistance) {
                return counter;
            }
        }
        return counter;
    }

    private LinkedHashMap<String, Double> sortMap(Map<String, Double> wordsMap) {
        LinkedHashMap<String, Double> newMap = new LinkedHashMap<>();
        ArrayList<Double> values = new ArrayList<>(wordsMap.values());
        Collections.sort(values);
        Collections.reverse(values);
        for(Double tmp : values){
            for(Map.Entry<String, Double> k : wordsMap.entrySet()) {
                if(tmp.equals(k.getValue())) {
                    newMap.put(k.getKey(), k.getValue());
                }
            }
        }

        return newMap;
    }
}
