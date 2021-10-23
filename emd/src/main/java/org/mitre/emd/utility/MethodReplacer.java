package org.mitre.emd.utility;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodReplacer extends ClassVisitor {
    private String mname;
    private String mdesc;
    private String cname;
    public MethodReplacer(ClassVisitor cv,
                          String mname) {
        super(7 << 16 | 0 << 8,cv);  // TODO This should be org.objectweb.asm.Opcodes.ASM7 but NetLogo uses ASM5 and we need to figure out how to have both 5 and 9.

        this.mname = mname;
    }
    public void visit(int version, int access,
                      String name, String signature,
                      String superName, String[] interfaces) {
        this.cname = name;
        cv.visit(version, access, name, signature, superName, interfaces);
    }

    public MethodVisitor visitMethod(int access,
                                     String name, String desc,
                                     String signature, String[] exceptions) {
        String newName = name;
        if(name.equals(mname)) {
            newName = "orig$" + name;
            generateNewBody(access, desc, signature,
                    exceptions, name, newName); }
        return super.visitMethod(access, newName, desc, signature, exceptions);
    }
    private void generateNewBody(int access, String desc, String signature, String[] exceptions,
                                 String name, String newName) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        mv.visitCode();
        // call original metod
        mv.visitVarInsn(Opcodes.ALOAD, 0); // this
        mv.visitMethodInsn(access, cname, newName,desc);
        mv.visitEnd();
    }
}
