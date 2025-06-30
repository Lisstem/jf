package net.lisstem.jf.compiler;

import net.lisstem.jf.Programm;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.InputStream;
import java.io.OutputStream;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.V9;

public class ProgrammWriter {
    public byte[] transform(String name, String code) {
        ClassWriter cw = new ClassWriter(COMPUTE_FRAMES & COMPUTE_MAXS);
        cw.visit(V9, Opcodes.ACC_PUBLIC, "bf/" + name, null, "java/lang/Object", new String[] {Type.getInternalName(Programm.class)});
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "run", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(InputStream.class), Type.getType(OutputStream.class)), null, null);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitEnd();
        cw.visitEnd();

        return cw.toByteArray();
    }
}
