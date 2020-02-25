package org.hawkular.alerts.api.model.condition;

import java.util.List;

import org.hawkular.alerts.api.doc.DocModel;
import org.hawkular.alerts.api.doc.DocModelProperty;
import org.hawkular.alerts.api.model.condition.Condition.Type;
import org.hawkular.alerts.api.model.condition.NelsonCondition.NelsonRule;
import org.hawkular.alerts.api.model.data.Data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * An evaluation state for nelson condition.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@DocModel(description = "An evaluation state for nelson condition.")
public class NelsonConditionEval extends ConditionEval {

    private static final long serialVersionUID = 1L;

    @DocModelProperty(description = "Mean applied to NelsonRules.", position = 0)
    @JsonInclude(Include.NON_NULL)
    private Double mean;

    @DocModelProperty(description = "Standard Deviation applied to NelsonRules.", position = 1)
    @JsonInclude(Include.NON_NULL)
    private Double standardDeviation;

    @DocModelProperty(description = "Data used to determine violations.", position = 2)
    @JsonInclude(Include.NON_NULL)
    private List<Data> violationsData;

    @DocModelProperty(description = "NelsonRule violations for the data.", position = 3)
    @JsonInclude(Include.NON_NULL)
    private List<NelsonRule> violations;

    /**
     * Used for JSON deserialization, not for general use.
     */
    public NelsonConditionEval() {
        super(Type.NELSON, false, 0, null);
    }

    public NelsonConditionEval(NelsonCondition condition, Data data, List<NelsonRule> violations, Double mean,
            Double standardDeviation, List<Data> violationsData) {
        super(Type.NELSON, condition.match(violations), data.getTimestamp(), data.getContext());
        setCondition(condition);
        this.mean = mean;
        this.standardDeviation = standardDeviation;
        this.violations = violations;
        this.violationsData = violationsData;
    }

    @Override
    public NelsonCondition getCondition() {
        return (NelsonCondition) condition;
    }

    public Double getMean() {
        return mean;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public Double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(Double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public List<Data> getViolationsData() {
        return violationsData;
    }

    public void setViolationsData(List<Data> violationsData) {
        this.violationsData = violationsData;
    }

    public List<NelsonRule> getViolations() {
        return violations;
    }

    public void setViolations(List<NelsonRule> violations) {
        this.violations = violations;
    }

    @Override
    public String getTenantId() {
        return condition.getTenantId();
    }

    @Override
    public String getTriggerId() {
        return condition.getTriggerId();
    }

    @Override
    public int getConditionSetSize() {
        return condition.getConditionSetSize();
    }

    @Override
    public int getConditionSetIndex() {
        return condition.getConditionSetIndex();
    }

    @Override
    public void updateDisplayString() {
        String s = String.format(
                "Nelson: %s violations=%s mean=%.2f, standardDeviation=%.2f, sampleSize=%d, violationsData=%s",
                condition.getDataId(), violations, mean, standardDeviation, getCondition().getSampleSize(), violationsData);
        setDisplayString(s);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((condition == null) ? 0 : condition.hashCode());
        result = prime * result + ((violations == null) ? 0 : violations.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        NelsonConditionEval other = (NelsonConditionEval) obj;
        if (condition == null) {
            if (other.condition != null)
                return false;
        } else if (!condition.equals(other.condition))
            return false;
        if (violations == null) {
            if (other.violations != null)
                return false;
        } else if (!violations.equals(other.violations))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NelsonConditionEval [condition=" + condition + ", mean=" + mean + ", standardDeviation="
                + standardDeviation + ", violations=" + violations + "]";
    }

}
