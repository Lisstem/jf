package net.lisstem.jf;

import net.lisstem.jf.compiler.ProgrammWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BfClassLoader extends ClassLoader {
    private final ProgrammWriter writer = new ProgrammWriter();
    private final String[] classPaths = System.getProperty("java.class.path").split(":");

    public BfClassLoader(ClassLoader parent) {
        super(parent);
    }
    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        String fileName = name.replace('.', File.separatorChar) + ".bf";
        for (String path : classPaths) {
            byte[] b = loadClassFromFile(name.replace('.', '/'), path + File.separator + fileName);
            if (b != null) {
                return defineClass(name, b, 0, b.length);
            }
        }

        return super.loadClass(name);
    }

    private byte[] loadClassFromFile(String className, String file) throws ClassNotFoundException  {
        try (FileInputStream input = new FileInputStream(file)) {
            return writer.transform(className, input);
        } catch (IOException e) {
            return null;
        }
    }
}
