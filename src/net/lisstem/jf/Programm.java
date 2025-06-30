package net.lisstem.jf;

import java.io.InputStream;
import java.io.OutputStream;

public interface Programm {
    void run(InputStream in, OutputStream out);
}
