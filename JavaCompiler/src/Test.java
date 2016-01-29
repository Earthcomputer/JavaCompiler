import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;

import net.earthcomputer.compiler.JavaCompilerExt;

public class Test {

	public static void main(String[] args) throws IOException {
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(null);
		JavaCompilerExt.compile(new BufferedReader(new FileReader(chooser.getSelectedFile())));
	}

}
