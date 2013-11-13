package edu.cmu.deiis.annotators;

import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.deiis.types.EntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * This is the newly added class, for named entity annotation, in hw3.
 * @author wwy
 */
public class NEAnnotator extends JCasAnnotator_ImplBase {
  private StanfordCoreNLP pipeline;

  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, lemma,ner");
    pipeline = new StanfordCoreNLP(props);
  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String documentText = aJCas.getDocumentText();

    Annotation document = new Annotation(documentText);
    pipeline.annotate(document);

    String preNe = "";
    int neBegin = 0;
    int neEnd = 0;

    // Iterate through the Stanford sentences
    for (CoreMap sent : document.get(SentencesAnnotation.class)) {
      // Iterate all Tokens
      for (CoreLabel token : sent.get(TokensAnnotation.class)) {
        int beginIndex = token.get(CharacterOffsetBeginAnnotation.class);
        int endIndex = token.get(CharacterOffsetEndAnnotation.class);

        // Add NER annotation
        String ne = token.get(NamedEntityTagAnnotation.class);
        if (ne != null) {
          if (ne.equals(preNe) && !preNe.equals("")) {
          } else if (preNe.equals("")) {
            neBegin = beginIndex;
            preNe = ne;
          } else {
            if (!preNe.equals("O")) {
              EntityMention sne = new EntityMention(aJCas);
              sne.setBegin(neBegin);
              sne.setEnd(neEnd);
              sne.setEntityType(preNe);
              sne.addToIndexes(aJCas);
            }
            // set the next named entity
            neBegin = beginIndex;
            preNe = ne;
          }
          neEnd = endIndex;
        }
      }
    }

  }

}
