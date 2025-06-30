package net.lisstem.jf;

import net.lisstem.jf.compiler.ProgrammWriter;

import java.io.FileOutputStream;
import java.io.IOException;

public class JF {
    private ProgrammWriter writer = new ProgrammWriter();


    public static void main(String[] args) throws IOException {
        JF jf = new JF();
        jf.compile("foobar");
    }

    public void compile(String file) throws IOException {
        try (FileOutputStream out = new FileOutputStream("test.class");) {
            out.write(this.writer.transform("test", "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++."));
        }
    }
}
