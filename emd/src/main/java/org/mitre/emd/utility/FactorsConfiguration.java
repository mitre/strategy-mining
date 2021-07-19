package org.mitre.emd.utility;

import java.util.List;
import java.util.Objects;

public class FactorsConfiguration {

    String className;
    String classPackage;
    String description;
    String returnType;
    List<String> childrenTypes;
    String evalMethod;

    FactorsConfiguration(){
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassPackage() {
        return classPackage;
    }

    public void setClassPackage(String class_package) {
        this.classPackage = classPackage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<String> getChildrenTypes() {
        return childrenTypes;
    }

    public void setChildrenTypes(List<String> childrenTypes) {
        this.childrenTypes = childrenTypes;
    }

    public String getEvalMethod() {
        return evalMethod;
    }

    public void setEvalMethod(String evalMethod) {
        this.evalMethod = evalMethod;
    }

    @Override
    public String toString() {
        return "FactorsConfiguration{" +
                "className='" + className + '\'' +
                ", packageName='" + classPackage + '\'' +
                ", description='" + description + '\'' +
                ", returnType='" + returnType + '\'' +
                ", childrenTypes=" + childrenTypes +
                ", evalMethod='" + evalMethod + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactorsConfiguration that = (FactorsConfiguration) o;
        return className.equals(that.className) &&
                classPackage.equals(that.classPackage) &&
                Objects.equals(description, that.description) &&
                returnType.equals(that.returnType) &&
                childrenTypes.equals(that.childrenTypes) &&
                evalMethod.equals(that.evalMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, classPackage, description, returnType, childrenTypes, evalMethod);
    }
}
