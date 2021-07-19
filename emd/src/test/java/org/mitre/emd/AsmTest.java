package org.mitre.emd;

import org.junit.Test;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.objectweb.asm.Opcodes.ASM7;

public class AsmTest {

//    @Test
    public void testMethodReplacer() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // MethodReplacer is adapted from the Kuleshov paper

        String classToClone = "Factor";  // This is the class you want to clone and put the method from the other class into.
        Class c = Factor.class;
        String className = c.getName();
        String classAsPath = className.replace('.', '/') + ".class";
        String classToRead = "FactorCopy";  // This is the class we read a method from
        String methodToRead = "method2";  // This is the method we want to read in classToRead
        String classToWrite = "NewFactor";  // Name of the new class we're writing

        ClassWriter cw = new ClassWriter(0);
        ClassReader cr = new ClassReader(Factor.class.getClassLoader().getResourceAsStream(classAsPath));
        ClassVisitor cv = new ClassVisitor(ASM7, cw) { };

        c = FactorCopy.class;
        className = c.getName();
        classAsPath = className.replace('.', '/') + ".class";

        ClassReader otherCr = new ClassReader(FactorCopy.class.getClassLoader().getResourceAsStream(classAsPath));
        ClassVisitor studentCv = new ClassVisitor(ASM7, cv) { };
        MethodReplacer mr = new MethodReplacer(studentCv, methodToRead, classToWrite);
        otherCr.accept(mr,0);

        cr.accept(cv,0);

        byte[] bytes = cw.toByteArray();
        writeClass(bytes,classToWrite);
        Class<?> newFactor = this.getClass().getClassLoader().loadClass("org.mitre.emd.rules.NewFactor");
        Method eval = newFactor.getClass().getMethod("eval");
        eval.invoke(newFactor);
    }

    public void writeClass(byte[] bytes, String className){
        String classLocation = "out/production/classes/org/mitre/emd/rules/";
        if(Path.of(classLocation).getParent().toString() != "emd"){  // TODO need to figure out how to make this work when packaged in a jar
            classLocation = "../emd/" + classLocation;
        }
        if (!Files.exists(Path.of(classLocation))){
            try {
                Files.createDirectories(Path.of(classLocation));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.write(Paths.get(classLocation, className + ".class"), bytes, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class Factor {
        String description = "Unknown Factor";

        public String toString() {
            return description;
        }

        public void eval(String state, int thread, boolean input) {

        }

        public int expectedChildren() {
            return 0;
        }

        public void setDescription(String description){
            this.description = description;
        }
    }

    public class MethodReplacer extends ClassVisitor {
        private String mname;
        private String mdesc;
        private String cname;
        public MethodReplacer(ClassVisitor cv,
                              String mname, String cname) {
            super(ASM7,cv);
            this.mname = mname;
            this.cname = cname;
            this.mdesc = mdesc;
        }
        public void visit(int version, int access,
                          String name, String signature,
                          String superName, String[] interfaces) {
//            this.cname = name;
            cv.visit(version, access, name, signature, superName, interfaces);
        }

        public MethodVisitor visitMethod(int access,
                                         String name, String desc,
                                         String signature, String[] exceptions) {
            String newName = name;
            if(name.equals(mname)) {
                newName = "eval";
                generateNewBody(access, desc, signature, exceptions, name, newName);

                return super.visitMethod(access, newName, desc, signature, exceptions);
            }
            return null;
//            return super.visitMethod(access, newName, desc, signature, exceptions);
        }

        private void generateNewBody(int access, String desc, String signature, String[] exceptions,
                                     String name, String newName) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            mv.visitCode();
            // call original metod
            mv.visitVarInsn(Opcodes.ALOAD, 0); // this
            mv.visitMethodInsn(access, cname, newName, desc);
            mv.visitEnd();
        }
    }

    public class FactorCopy {
        Integer mult = 2;

        public void method2(){
            int i = mult;
            long currentTimeTimes2 = System.currentTimeMillis() * i;
            System.out.println(currentTimeTimes2);
            System.out.println("Method 2 from " + this);
        }

        public void method3(){
            System.out.println("Method 3 from " + this);
        }
    }
}


