package coriant.cats.utils;

import java.io.PrintStream;

public class Dumper2 {
	public static void main(String[] argv) {
/*
	int[] num;
	int m = 1000;
	num = new int[m];
	for (int i=0; i<m; i++) num[i] = i+1;
	int n = 0;
	int j = 0;
	boolean eat = true;
	while (n<m) {
		while (num[j] ==0) j = (j+1)%m;
		if (eat) {
			System.out.print(num[j]+" ");
			num[j] = 0;
			n++;
			eat = false;
		} else {
			j = (j+1)%m;
			eat = true;
		}
	}
		*/
		
		
		int i = 0;
		
		ConsoleInput in = new ConsoleInput();
		PrintStream out = System.out;
		while (true) {
			String ret = in.getLine(out, "Hi, I am dumper "+(i++) +" -> \n");
			if (ret.startsWith("exit")) break;
			out.println("Your input is: '"+ret+"'\n");
			//file.writelf(ret);
			//System.out.println((i++)+":-> ");
			//ThreadUtil.sleep(1);
		}
		//file.close();
	}
}
