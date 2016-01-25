import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import javax.swing.JFileChooser;

import net.earthcomputer.compiler.JavaCompilerExt;
import net.earthcomputer.compiler.internal.UnicodeReplacerReader;

public class Test {

	public static void main(String[] args) throws IOException {
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(null);
		JavaCompilerExt.compile(new BufferedReader(new FileReader(chooser.getSelectedFile())));
		
		Scanner scanner = new Scanner(new UnicodeReplacerReader(new InputStreamReader(System.in)));
		while(true) {
			System.out.println(scanner.nextLine());
		}
	}

}
