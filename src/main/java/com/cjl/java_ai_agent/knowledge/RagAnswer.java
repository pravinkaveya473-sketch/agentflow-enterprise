package com.cjl.java_ai_agent.knowledge;
import java.util.List;
public record RagAnswer(String answer,List<Citation> citations){
 public record Citation(String documentId,String filename,Double score,String excerpt){}
}
