package com.cjl.java_ai_agent.evaluation;
import jakarta.persistence.*;import java.math.BigDecimal;import java.time.Instant;
@Entity @Table(name="evaluation_run")
public class EvaluationRunEntity{
 @Id private String id;@Column(name="evaluation_type",nullable=false)private String evaluationType;@Column(nullable=false)private String status;
 @Column(name="total_cases",nullable=false)private int totalCases;@Column(name="passed_cases",nullable=false)private int passedCases;
 @Column(name="pass_rate",nullable=false,precision=8,scale=4)private BigDecimal passRate;@Column(name="result_json",nullable=false,columnDefinition="MEDIUMTEXT")private String resultJson;
 @Column(name="started_by",nullable=false)private String startedBy;@Column(name="started_at",nullable=false)private Instant startedAt;@Column(name="completed_at",nullable=false)private Instant completedAt;
 protected EvaluationRunEntity(){} public EvaluationRunEntity(String id,String type,int total,int passed,String json,String by,Instant start,Instant end){this.id=id;this.evaluationType=type;this.status="COMPLETED";this.totalCases=total;this.passedCases=passed;this.passRate=total==0?BigDecimal.ZERO:BigDecimal.valueOf((double)passed/total).setScale(4,java.math.RoundingMode.HALF_UP);this.resultJson=json;this.startedBy=by;this.startedAt=start;this.completedAt=end;}
 public String getId(){return id;}public String getEvaluationType(){return evaluationType;}public String getStatus(){return status;}public int getTotalCases(){return totalCases;}public int getPassedCases(){return passedCases;}public BigDecimal getPassRate(){return passRate;}public String getResultJson(){return resultJson;}public String getStartedBy(){return startedBy;}public Instant getStartedAt(){return startedAt;}public Instant getCompletedAt(){return completedAt;}
}
