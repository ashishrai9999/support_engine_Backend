package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.apache.lucene.analysis.en.EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;

public class TextPreprocessor {

    public static String preprocess(String text) throws Exception {
        Analyzer analyzer = new StopAnalyzer(ENGLISH_STOP_WORDS_SET);
        TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(text));
        tokenStream = new PorterStemFilter(tokenStream); // Apply stemming

        List<String> result = new ArrayList<>();
        CharTermAttribute charTermAttr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            result.add(charTermAttr.toString());
        }
        tokenStream.end();
        tokenStream.close();
        analyzer.close();

        return String.join(" ", result);
    }

    public static void main(String[] args) throws Exception {
        String input = "The quick brown foxes are jumping over the lazy dogs.";
        String processed = preprocess(input);
        System.out.println(processed); // Output: quick brown fox jump lazi dog
    }
}
