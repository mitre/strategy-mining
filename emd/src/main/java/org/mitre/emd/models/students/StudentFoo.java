package org.mitre.emd.models.students;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;

public class StudentFoo {
//    Integer mult = 2;

    public void method2(){
//        int i = mult;
        int i = 2;
        long currentTimeTimes2 = System.currentTimeMillis() * i;
        System.out.println(currentTimeTimes2);
        System.out.println("Method 2 from " + this);
    }

    public void foo(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
//        int i = mult;
        int i = 2;
        long currentTimeTimes2 = System.currentTimeMillis() * i;
        System.out.println(currentTimeTimes2);
        System.out.println("Method 2 from " + this);
    }

    public void method3(){
        System.out.println("Method 3 from " + this);
    }
}
