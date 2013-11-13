package edu.cmu.deiis.cpe;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.deiis.annotators.ScoreComparator;
import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.Question;

/**
 * This class is for calculating the precision @ N
 * @author wwy
 */
public class Evaluation extends CasConsumer_ImplBase {
  double sumPrecision = 0.0;
  int countQuestion = 0;

  @Override
  public void processCas(CAS arg0) throws ResourceProcessException {
    JCas aJCas = null;
    try {
      aJCas = arg0.getJCas();
    } catch (CASException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Question ques = null;
    List<Answer> ansList = new ArrayList<Answer>();

    // iterate questions
    FSIndex<?> questionIndex = aJCas.getAnnotationIndex(Question.type);
    Iterator<?> questionIter = questionIndex.iterator();
    while (questionIter.hasNext()) {
      ques = (Question) questionIter.next();
      System.out.println(String.format("Question: %s", ques.getCoveredText()));

      // iterate answers
      FSIndex<?> answerIndex = aJCas.getAnnotationIndex(Answer.type);
      Iterator<?> answerIter = answerIndex.iterator();
      Answer answer;
      while (answerIter.hasNext()) {
        answer = (Answer) answerIter.next();
        ansList.add(answer);
      }
    }

    // calculate precision@N
    double N = 0.0;
    for (Answer answer : ansList) {
      if (answer.getIsCorrect()) {
        N++;
      }
    }

    int countCorrect = 0;
    for (int i = 0; i < N; i++) {
      if (ansList.get(i).getIsCorrect()) {
        countCorrect++;
      }
    }

    // sort scores of answers
    Collections.sort(ansList, new ScoreComparator());
    for (Answer answer : ansList) {
      String correctInd = null;
      if (answer.getIsCorrect() == true) {
        correctInd = "+";
      } else {
        correctInd = "-";
      }
      System.out.println(String.format("%s %.2f %s", correctInd, answer.getConfidence(),
              answer.getCoveredText()));
    }
    double precision = countCorrect / N;
    System.out.println(String.format("Precision at %.1f: %.2f ", N, precision));
    sumPrecision += precision;
    countQuestion +=1 ;
  }
  
  @Override
  public void collectionProcessComplete(ProcessTrace aTrace)
          throws ResourceProcessException, IOException  {
    System.out.println(String.format("Average Precision: %.2f", sumPrecision/countQuestion));
  }
}
