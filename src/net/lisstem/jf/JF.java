package net.lisstem.jf;

import net.lisstem.jf.compiler.ProgrammWriter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class JF {
    private ProgrammWriter writer = new ProgrammWriter();


    public static void main(String[] args) throws IOException {
        JF jf = new JF();
        Arrays.stream(args).forEach(arg -> {
            try {
                jf.compile(arg);
            } catch (IOException e) {
                System.out.println("Cloud not transform " + arg);
                e.printStackTrace();
            }
        });
    }

    public void compile(String file) throws IOException {
        Path path = Paths.get(file);
        String bfName = this.toBfClassname(path);
        Path outPath = Paths.get("bf", bfName + ".class");
        try (
                FileOutputStream out = new FileOutputStream(outPath.toFile());
                FileInputStream in  = new FileInputStream(path.toFile()))
        {
            out.write(this.writer.transform(bfName, in));
        }
    }

    public String toBfClassname(Path path) {
        return path.getFileName().toString().split("\\.")[0];
    }
}
