package org.mitre.emd.utility;

import java.util.List;
import java.util.Objects;

public class EmdConfiguration {
    protected List<FactorsConfiguration> factors;

    public EmdConfiguration(){
    }

    public List<FactorsConfiguration> getFactors() {
        return factors;
    }

    public void setFactors(List<FactorsConfiguration> factors) {
        this.factors = factors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmdConfiguration that = (EmdConfiguration) o;
        return Objects.equals(factors, that.factors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(factors);
    }
}
