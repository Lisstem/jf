package net.lisstem.jf.test;

import net.lisstem.jf.BfClassLoader;
import net.lisstem.jf.Programm;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ClassLoader loader = new BfClassLoader(Main.class.getClassLoader());
        Class<? extends Programm> clazz = (Class<? extends Programm>) loader.loadClass("net.lisstem.jf.test.HelloWorld");
        Programm programm = clazz.newInstance();
        programm.run(System.in, System.out);
    }
}
