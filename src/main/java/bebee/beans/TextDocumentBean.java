package bebee.beans;

import bebee.pojo.TextDocument;
import bebee.utils.StopWords;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.*;

/**
 * Created by Rivas on 9/5/17.
 */
public class TextDocumentBean {

    static Logger logger = Logger.getLogger(TextDocumentBean.class);

    private TextDocument textDocument;

    public TextDocument getTextDocument() {
        return textDocument;
    }

    public void setTextDocument(TextDocument textDocument) {
        this.textDocument = textDocument;
    }

    public TextDocumentBean(TextDocument textDocument) {
        this.textDocument = textDocument;
    }

    /**
     * Método que filtra un texto devolviendo como resultado un mapa con las palabras más relevantes
     * y el número de veces que aparece cada una.
     * Para el procesado de las palabras mas relevantes se aplica un filtrado siguiendo los pasos:
     * 1. Eliminado de signos de puntuación
     * 2. Se calcula el lema del diccionario de stop words
     * 3. Se calcula el lema de las palabras del texto
     * 4. Se eliminan las stop words
     * 5. Finalmente se devuelve el mapa con las palabras (lematizadas) más relevantes y el numero de veces.
     *
     * @return
     */
    public Map<String, Double> filterDocument(){
        Map<String, Double> wordsMap = new HashMap<>();
        LinkedHashMap<String, Double> orderedMap = new LinkedHashMap<>();
        InputStream inputStream = textDocument.getInputStream();
        try {
            String document = "";
            document = IOUtils.toString(inputStream, "UTF-8");
            String[] wordsList = parsePunctuation(document);
            List<String> stopWordsDictionary = stemArray(StopWords.SPANISH, true);
            List<String> stemmedWordsList = stemArray(wordsList, false);
            removeStopWords(stemmedWordsList, stopWordsDictionary);

            double total = stemmedWordsList.size(); //Numero palabras totales del documento
            double temp = 1/total;
            double unit = temp*100; //Valor de una unidad, en porcentaje sobre el numero de palabras del doc.
            for(String word : stemmedWordsList) {
                double newValue;
                if (wordsMap.containsKey(word)) {
                    double oldValue = wordsMap.get(word);
                    newValue = oldValue+unit;
                } else {
                    newValue = unit;
                }

                if(word.length()>3) {
                    wordsMap.put(word, newValue);
                }
            }
            orderedMap = sortWordsMap(wordsMap);

        }catch(Exception e){
            logger.error("Error procesando el documento.");
        }

        return orderedMap;
    }

    private LinkedHashMap<String, Double> sortWordsMap(Map<String, Double> wordsMap) {
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

    private List<String> stemArray(String[] array, boolean isDictionary){
        List<String> result = new ArrayList<>();
        for (String word : array) {
            word = removeAccent(word);
            word = stem(word);
            // Ojo! Si no estamos lematizando un diccionario la palabra se añade siempre a la lista, aunque se duplique
            // en otro caso cada vez que se repita una palabra solo se almacena una vez...
            // En otro caso, si lo que estamos lematizando es un diccionario no se almacenan los duplicados
            // ya que son redundantes.
            if(!isDictionary || !result.contains(word) ) {
                result.add(word);
            }
        }

        return result;
    }

    /**
     * Parsea el documento y elimina los signos de puntiación.
     *
     * @param document
     * @return
     */
    private String[] parsePunctuation(String document){
        List<String> wordsList = new ArrayList<>();
        String[] words = document.replaceAll("[^a-zA-Zá-ú\\n ]", "").toLowerCase().split("\\s+");
        wordsList.addAll(Arrays.asList(words));

        return words;
    }

    /**
     * Elimina las StopWords del documento
     *
     * @param wordsList
     * @return
     */
    private void removeStopWords(List<String> wordsList, List<String> stopWordsDictionary){
        for (String stopword : stopWordsDictionary) {
            if (wordsList.contains(stopword)) {
                boolean flag;
                do{
                    flag = wordsList.remove(stopword);
                }while(flag);
            }
        }
    }

    /**
     * Procesa una palabra y devuelve su raíz.
     *
     * @param word
     * @return
     */
    private String stem(String word){
        int len = word.length();
        if (len <= 3) {
            return removeAccent(word);
        }

        word = word.toLowerCase();

        int r1, r2, rv;
        r1 = r2 = rv = len;

        // R1 is the region after the first non-vowel following a vowel, or is
        // the null region at the end of the word if there is no such non-vowel.
        for (int i = 0; i < (len - 1) && r1 == len; i++) {
            if (is_vowel(word.charAt(i)) && !is_vowel(word.charAt(i + 1))) {
                r1 = i + 2;
            }
        }

        // R2 is the region after the first non-vowel following a vowel in R1,
        // or is the null region at the end of the word if there is no such
        // non-vowel.
        for (int i = r1; i < (len - 1) && r2 == len; i++) {
            if (is_vowel(word.charAt(i)) && !is_vowel(word.charAt(i + 1))) {
                r2 = i + 2;
            }
        }

        if (len > 3) {
            if (!is_vowel(word.charAt(1))) {
                // If the second letter is a consonant, RV is the region after
                // the next following vowel
                rv = getNextVowelPos(word, 2) + 1;
            } else if (is_vowel(word.charAt(0)) && is_vowel(word.charAt(1))) {
                // or if the first two letters are vowels, RV is the region
                // after the next consonant
                rv = getNextConsonantPos(word, 2) + 1;
            } else {
                // otherwise (consonant-vowel case) RV is the region after the
                // third letter. But RV is the end of the word if these
                // positions cannot be found.
                rv = 3;
            }
        }

        String r1_txt = substr(word, r1);
        String r2_txt = substr(word, r2);
        String rv_txt = substr(word, rv);

        String word_orig = word;

        // Step 0: Attached pronoun
        String[] pronoun_suf = array("me", "se", "sela", "selo", "selas",
                "selos", "la", "le", "lo", "las", "les", "los", "nos");
        String[] pronoun_suf_pre1 = array("iéndo", "ándo", "ár", "ér", "ír");
        String[] pronoun_suf_pre2 = array("ando", "iendo", "ar", "er", "ir");
        String suf = endsinArr(word, pronoun_suf);

        if (!suf.equals("")) {
            String pre_suff = endsinArr(substr(rv_txt, 0, -strlen(suf)),
                    pronoun_suf_pre1);
            if (!pre_suff.equals("")) {
                word = removeAccent(substr(word, 0, -strlen(suf)));
            } else {
                pre_suff = endsinArr(substr(rv_txt, 0, -strlen(suf)),
                        pronoun_suf_pre2);
                if (!pre_suff.equals("")
                        || (endsin(word, "yendo") && (substr(word,
                        -strlen(suf) - 6, 1).equals("u")))) {
                    word = substr(word, 0, -strlen(suf));
                }
            }
        }

        if (!word.equals(word_orig)) {
            r1_txt = substr(word, r1);
            r2_txt = substr(word, r2);
            rv_txt = substr(word, rv);
        }
        String word_after0 = word;

        if (!(suf = endsinArr(r2_txt, array("anza", "anzas", "ico", "ica",
                "icos", "icas", "ismo", "ismos", "able", "ables", "ible",
                "ibles", "ista", "istas", "oso", "osa", "osos", "osas",
                "amiento", "amientos", "imiento", "imientos"))).equals("")) {
            word = substr(word, 0, -strlen(suf));
        } else if (!(suf = endsinArr(r2_txt, array("icadora", "icador",
                "icación", "icadoras", "icadores", "icaciones", "icante",
                "icantes", "icancia", "icancias", "adora", "ador", "ación",
                "adoras", "adores", "aciones", "ante", "antes", "ancia", "ancias"))).equals("")) {
            word = substr(word, 0, -strlen(suf));
        } else if (!(suf = endsinArr(r2_txt, array("logía", "logías"))).equals("")) {
            word = substr(word, 0, -strlen(suf)) + "log";
        } else if (!(suf = endsinArr(r2_txt, array("ución", "uciones"))).equals("")) {
            word = substr(word, 0, -strlen(suf)) + "u";
        } else if (!(suf = endsinArr(r2_txt, array("encia", "encias"))).equals("")) {
            word = substr(word, 0, -strlen(suf)) + "ente";
        } else if (!(suf = endsinArr(r2_txt, array("ativamente", "ivamente",
                "osamente", "icamente", "adamente"))).equals("")) {
            word = substr(word, 0, -strlen(suf));
        } else if (!(suf = endsinArr(r1_txt, array("amente"))).equals("")) {
            word = substr(word, 0, -strlen(suf));
        } else if (!(suf = endsinArr(r2_txt, array("antemente", "ablemente",
                "iblemente", "mente"))).equals("")) {
            word = substr(word, 0, -strlen(suf));
        } else if (!(suf = endsinArr(r2_txt, array("abilidad", "abilidades",
                "icidad", "icidades", "ividad", "ividades", "idad", "idades"))).equals("")) {
            word = substr(word, 0, -strlen(suf));
        } else if (!(suf = endsinArr(r2_txt, array("ativa", "ativo", "ativas",
                "ativos", "iva", "ivo", "ivas", "ivos"))).equals("")) {
            word = substr(word, 0, -strlen(suf));
        }

        if (!word.equals(word_after0)) {
            r1_txt = substr(word, r1);
            r2_txt = substr(word, r2);
            rv_txt = substr(word, rv);
        }
        String word_after1 = word;

        if (word_after0.equals(word_after1)) {
            // Do step 2a if no ending was removed by step 1.
            if ((!(suf = endsinArr(rv_txt,
                    array("ya", "ye", "yan", "yen", "yeron", "yendo", "yo",
                            "yó", "yas", "yes", "yais", "yamos"))).equals(""))
                    && (substr(word, -strlen(suf) - 1, 1).equals("u"))) {
                word = substr(word, 0, -strlen(suf));
            }

            if (!word.equals(word_after1)) {
                r1_txt = substr(word, r1);
                r2_txt = substr(word, r2);
                rv_txt = substr(word, rv);
            }
            String word_after2a = word;

            // Do Step 2b if step 2a was done, but failed to remove a suffix.
            if (word_after2a.equals(word_after1)) {
                if (!(suf = endsinArr(rv_txt, array("arían", "arías",
                        "arán", "arás", "aríais", "aría", "aréis", "aríamos",
                        "aremos", "ará", "aré", "erían", "erías", "erán",
                        "erás", "eríais", "ería", "eréis", "eríamos", "eremos",
                        "erá", "eré", "irían", "irías", "irán", "irás",
                        "iríais", "iría", "iréis", "iríamos", "iremos", "irá",
                        "iré", "aba", "ada", "ida", "ía", "ara", "iera", "ad",
                        "ed", "id", "ase", "iese", "aste", "iste", "an",
                        "aban", "ían", "aran", "ieran", "asen", "iesen",
                        "aron", "ieron", "ado", "ido", "ando", "iendo", "ió",
                        "ar", "er", "ir", "as", "abas", "adas", "idas", "ías",
                        "aras", "ieras", "ases", "ieses", "ís", "áis", "abais",
                        "íais", "arais", "ierais", "aseis", "ieseis", "asteis",
                        "isteis", "ados", "idos", "amos", "ábamos", "íamos",
                        "imos", "áramos", "iéramos", "iésemos", "ásemos"))).equals("")) {
                    word = substr(word, 0, -strlen(suf));
                } else if (!(suf = endsinArr(rv_txt, array("en", "es", "éis", "emos"))).equals("")) {
                    word = substr(word, 0, -strlen(suf));
                    if (endsin(word, "gu")) {
                        word = substr(word, 0, -1);
                    }
                }
            }
        }

        // Always do step 3.
        r1_txt = substr(word, r1);
        r2_txt = substr(word, r2);
        rv_txt = substr(word, rv);

        if (!(suf = endsinArr(rv_txt, array("os", "a", "o", "á", "í", "ó"))).equals("")) {
            word = substr(word, 0, -strlen(suf));
        } else if (!(suf = endsinArr(rv_txt, array("e", "é"))).equals("")) {
            word = substr(word, 0, -1);
            rv_txt = substr(word, rv);
            if (endsin(rv_txt, "u") && endsin(word, "gu")) {
                word = substr(word, 0, -1);
            }
        }

        return removeAccent(word);
    }

    /**
     * Elimina los acentos de una palabra
     *
     * @param word palabra
     * @return
     */
    private String removeAccent(String word) {
        word = word.replace('á', 'a');
        word = word.replace('é', 'e');
        word = word.replace('í', 'i');
        word = word.replace('ó', 'o');
        word = word.replace('ú', 'u');
        return word;
    }

    /**
     * Determina si el caracter es una vocal.
     *
     * @param c
     * @return
     */
    private boolean is_vowel(char c) {
        return (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u'
                || c == 'á' || c == 'é' || c == 'í' || c == 'ó' || c == 'ú');
    }

    /**
     * Retorna la posicion de la primera vocal encontrada
     *
     * @param word
     * @param start
     * @return
     */
    private int getNextVowelPos(String word, int start) {
        int len = word.length();
        for (int i = start; i < len; i++) {
            if (is_vowel(word.charAt(i))) {
                return i;
            }
        }
        return len;
    }

    /**
     * Retorna la posicion de la primera consonante encontrada
     *
     * @param word
     * @param start
     * @return
     */
    private int getNextConsonantPos(String word, int start) {
        int len = word.length();
        for (int i = start; i < len; i++) {
            if (!is_vowel(word.charAt(i))) {
                return i;
            }
        }
        return len;
    }

    /**
     * Retorna una subcadena de un String
     */
    private String substr(String word, int beginIndex) {
        if (Math.abs(beginIndex) > word.length()) {
            return word;
        } else if (beginIndex >= 0) {
            return word.substring(beginIndex, word.length());
        } else {
            return word.substring(word.length() + beginIndex, word.length());
        }
    }

    /**
     * Retorna un array de String dados muchos Strings
     */
    private String[] array(String... strings) {
        return strings;
    }

    /**
     * Determina si el sufijo esta presente en una palabra
     */
    private boolean endsin(String word, String suffix) {
        if (word.length() < suffix.length()) {
            return false;
        }
        return (substr(word, -suffix.length()).equals(suffix));
    }

    /**
     * Retorna el sufijo mas largo presente en una palabra
     */
    private String endsinArr(String word, String[] suffixes) {
        String tmp = "";
        for (String suff : suffixes) {
            if (endsin(word, suff)) {
                if (suff.length() >= tmp.length()) {
                    tmp = suff;
                }
            }
        }
        return tmp;
    }

    /**
     * Retorna la longitud de una palabra
     */
    private int strlen(String word) {
        return word.length();
    }

    /**
     *Retorna una subcadena de un String
     */
    private String substr(String word, int beginIndex, int length) {
        if (beginIndex == length) {
            return "";

        } else {

            if ((beginIndex >= 0)) { // incio positivo
                int endIndex;
                if ((length >= 0)) { // longitud positiva
                    endIndex = beginIndex + length;
                    if (endIndex > word.length()) {
                        word = word.substring(beginIndex, word.length());
                        return word;
                    } else {
                        word = word.substring(beginIndex, endIndex);
                        return word;
                    }
                } else { // longitud negativa
                    endIndex = word.length() + length;
                    try {
                        word = word.substring(beginIndex, endIndex);
                    } catch (StringIndexOutOfBoundsException e) {
                        word = "";
                    }
                    return word;
                }

            } else {// incio negativo
                int endIndex;
                int newBeginIndex;
                if ((length >= 0)) { // longitud positiva
                    newBeginIndex = word.length() + beginIndex;
                    endIndex = newBeginIndex + length;
                    if (endIndex > word.length()) {
                        word = word.substring(newBeginIndex, word.length());
                        return word;
                    } else {
                        word = word.substring(newBeginIndex, endIndex);
                        return word;
                    }
                } else { // longitud negativa
                    newBeginIndex = word.length() + beginIndex;
                    endIndex = word.length() + length;

                    try {
                        word = word.substring(newBeginIndex, endIndex);
                    } catch (StringIndexOutOfBoundsException e) {
                        word = "";
                    }

                    return word;
                }
            }

        }

    }
}
