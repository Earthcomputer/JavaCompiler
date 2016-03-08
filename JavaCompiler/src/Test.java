import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import net.earthcomputer.compiler.JavaCompilerExt;

public class Test {

	public static void main(String[] args) throws IOException {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		chooser.setDialogTitle("Input file");
		chooser.showOpenDialog(null);
		File inputFile = chooser.getSelectedFile();
		System.out.println("Input file: " + inputFile);
		
		chooser.setDialogTitle("Output file");
		chooser.setCurrentDirectory(null);
		chooser.showOpenDialog(null);
		File outputFile = chooser.getSelectedFile();
		System.out.println("Output file: " + outputFile);
		
		JavaCompilerExt.compile(inputFile, outputFile);
	}

}
