package net.lisstem.jf.compiler;

import net.lisstem.jf.Programm;
import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.V9;

public class ProgrammWriter {
    public static final int TAPE_SIZE = 1024 * 1024;

    public byte[] transform(String name, FileInputStream file) throws IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = cw;
        cv.visit(V9, Opcodes.ACC_PUBLIC, name, null, "java/lang/Object", new String[] {Type.getInternalName(Programm.class)});
        this.createInit(cv);
        this.createRun(cv, file);
        this.createMain(name, cv);
        cv.visitEnd();

        return cw.toByteArray();
    }

    private void createInit(ClassVisitor cv) {
        MethodVisitor init = cv.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        init.visitInsn(Opcodes.RETURN);
        init.visitMaxs(1, 1);
        init.visitEnd();
    }

    public static final int TAPE_POSITION = 4;
    public static final int DATA_POINTER_POSITION = 3;
    public static final int OUT_STREAM_POSITION = 2;
    public static final int IN_STREAM_POSITION = 1;

    private void createRun(ClassVisitor cv, FileInputStream file) throws IOException {
        MethodVisitor run = cv.visitMethod(Opcodes.ACC_PUBLIC, "run", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(InputStream.class), Type.getType(OutputStream.class)), null, null);
        run.visitCode();
        run.visitLdcInsn(TAPE_SIZE);
        run.visitInsn(Opcodes.ICONST_0);
        run.visitVarInsn(Opcodes.ISTORE, DATA_POINTER_POSITION);
        run.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
        run.visitVarInsn(Opcodes.ASTORE, TAPE_POSITION);
        Stack<Label> loopStarts = new Stack<>();
        Stack<Label> loopEnds = new Stack<>();
        int b;
        while ((b = file.read()) >= 0) {
            /*
            [ 	If the byte at the data pointer is zero, then instead of moving the instruction pointer forward to the next command, jump it forward to the command after the matching ] command.
            ] 	If the byte at the data pointer is nonzero, then instead of moving the instruction pointer forward to the next command, jump it back to the command after the matching [ command.[a]
             */
            switch (b) {
                case '>':
                    run.visitIincInsn(DATA_POINTER_POSITION, 1);
                    break;
                case '<':
                    run.visitIincInsn(DATA_POINTER_POSITION, -1);
                    break;
                case '+':
                    run.visitVarInsn(Opcodes.ALOAD, TAPE_POSITION);
                    run.visitVarInsn(Opcodes.ILOAD, DATA_POINTER_POSITION);
                    run.visitInsn(Opcodes.DUP2);
                    run.visitInsn(Opcodes.BALOAD);
                    run.visitInsn(Opcodes.ICONST_1);
                    run.visitInsn(Opcodes.IADD);
                    run.visitInsn(Opcodes.BASTORE);
                    break;
                case '-':
                    run.visitVarInsn(Opcodes.ALOAD, TAPE_POSITION);
                    run.visitVarInsn(Opcodes.ILOAD, DATA_POINTER_POSITION);
                    run.visitInsn(Opcodes.DUP2);
                    run.visitInsn(Opcodes.BALOAD);
                    run.visitInsn(Opcodes.ICONST_M1);
                    run.visitInsn(Opcodes.IADD);
                    run.visitInsn(Opcodes.BASTORE);
                    break;
                case '.':
                    run.visitVarInsn(Opcodes.ALOAD, OUT_STREAM_POSITION);
                    run.visitVarInsn(Opcodes.ALOAD, TAPE_POSITION);
                    run.visitVarInsn(Opcodes.ILOAD, DATA_POINTER_POSITION);
                    run.visitInsn(Opcodes.BALOAD);
                    run.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getType(OutputStream.class).getInternalName(), "write", Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE));
                    break;
                case ',':
                    run.visitVarInsn(Opcodes.ALOAD, TAPE_POSITION);
                    run.visitVarInsn(Opcodes.ILOAD, DATA_POINTER_POSITION);
                    run.visitVarInsn(Opcodes.ALOAD, IN_STREAM_POSITION);
                    run.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getType(InputStream.class).getInternalName(), "read", Type.getMethodDescriptor(Type.INT_TYPE));
                    run.visitInsn(Opcodes.BASTORE);
                    break;
                case '[':
                    Label loopStart = new Label();
                    loopStarts.push(loopStart);
                    Label loopEnd = new Label();
                    loopEnds.push(loopEnd);
                    run.visitVarInsn(Opcodes.ALOAD, TAPE_POSITION);
                    run.visitVarInsn(Opcodes.ILOAD, DATA_POINTER_POSITION);
                    run.visitInsn(Opcodes.BALOAD);
                    run.visitInsn(Opcodes.ICONST_0);
                    run.visitJumpInsn(Opcodes.IF_ICMPEQ, loopEnd);
                    run.visitLabel(loopStart);
                    break;
                case ']':
                    run.visitVarInsn(Opcodes.ALOAD, TAPE_POSITION);
                    run.visitVarInsn(Opcodes.ILOAD, DATA_POINTER_POSITION);
                    run.visitInsn(Opcodes.BALOAD);
                    run.visitInsn(Opcodes.ICONST_0);
                    run.visitJumpInsn(Opcodes.IF_ICMPNE, loopStarts.pop());
                    run.visitLabel(loopEnds.pop());
                    break;
            }
        }
        run.visitInsn(Opcodes.RETURN);
        run.visitMaxs(4, 5);
        run.visitEnd();
    }

    private void createMain(String name, ClassVisitor cv) {
        MethodVisitor main = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        main.visitCode();
        main.visitTypeInsn(Opcodes.NEW, name);
        main.visitInsn(Opcodes.DUP);
        main.visitMethodInsn(Opcodes.INVOKESPECIAL, name, "<init>", "()V", false);
        main.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        main.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        main.visitMethodInsn(Opcodes.INVOKEVIRTUAL, name, "run", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(InputStream.class), Type.getType(OutputStream.class)), false);
        main.visitInsn(Opcodes.RETURN);
        main.visitMaxs(3, 1);
        main.visitEnd();
    }
}
