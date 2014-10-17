import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.lucene.analysis.vi.VietnameseTokenizer;

import java.io.IOException;
import java.io.StringReader;
import java.text.BreakIterator;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author duydo
 */
public class AppE {

    public static Properties getTokenizerProperties() throws IOException {
        final Properties properties = new Properties();
        properties.load(AppE.class.getResourceAsStream("/vi-tokenizer.properties"));
        Enumeration e = properties.propertyNames();
        final Properties tokenizerProperties = new Properties();
        for (; e.hasMoreElements(); ) {
            final Object key = e.nextElement();
            final String value = String.valueOf(properties.get(key));
           tokenizerProperties.put(key, AppE.class.getResource(value).getFile());
        }
        return tokenizerProperties;
    }


    static void extractWords(String target, BreakIterator wordIterator) {

        wordIterator.setText(target);
        int start = wordIterator.first();
        int end = wordIterator.next();

        while (end != BreakIterator.DONE) {
            String word = target.substring(start,end);
            if (Character.isLetterOrDigit(word.charAt(0))) {
                System.out.println(word);
            }
            start = end;
            end = wordIterator.next();
        }
    }
    public static void main(String[] args) throws Exception {
        String text = "Gia tăng đóng góp của ASEM trước những chuyển biến khôn lường";
        VietnameseTokenizer tokenizer = new VietnameseTokenizer(new StringReader(text));
//        StandardTokenizer tokenizer = new StandardTokenizer(Version.LUCENE_4_9, new StringReader(text));

        CharTermAttribute termAttribute = tokenizer.getAttribute(CharTermAttribute.class);

        while (tokenizer.incrementToken()) {
            System.out.println(termAttribute);
        }


    }
}
