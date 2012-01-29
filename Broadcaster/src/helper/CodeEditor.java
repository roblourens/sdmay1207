package helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


public class CodeEditor {
	public static void main(String args[]){
		CodeEditor editor = new CodeEditor();
		
		if(args.length!=1){
				System.out.println("ERROR\nArg[0]:"+args[0]);
				return;
		}
		
		File dir = new File(args[0]);
		File [] files;
		if(dir.isDirectory()){
			files = dir.listFiles();
		} else {
			files = new File[1];
			files[0] = dir;
		}

		for(File f : files){
			String name = f.toString();
			System.out.println("File: "+name);
			File out = new File("out.txt");
			File newFile = new File(f+".java");
			editor.spaceRemover(f,out);
			editor.removeNumbers(out, newFile);
			out.delete();
		}
	}
	
	private void removeNumbers(File fin, File fout){
		System.out.println("In Number Remover");
		try{
			FileInputStream fis = new FileInputStream(fin);
			FileOutputStream fos = new FileOutputStream(fout);
			byte[] buf = new byte[512];
			int amount = 0;
			while((amount = fis.read(buf))>=0){
				for(int i=0;i<amount;i++){
					if(!isNumber(buf[i])){
						fos.write(buf[i]);
					} else 	if(i>0 && buf[i-1]==0x0A){
						i+=1;
						while(i<amount-1 && isNumber(buf[i])){
							i+=1;
						}
						//if(buf[i]==0x09) i+=1;
					} else {
						fos.write(buf[i]);
					}
				}
			}
			
		} catch (Exception e){
			System.out.println("\tERROR HERE IN SPACEREMOVER");
			e.printStackTrace();
		}
		System.out.println("Exiting Number Remover");
		
	}
	private boolean isNumber(byte b){
		return (b>=0x30 && b<=0x39);
	}
	
	private void spaceRemover(File fin, File fout){
		System.out.println("In Space Remover");
		try{
			FileInputStream fis = new FileInputStream(fin);
			FileOutputStream fos = new FileOutputStream(fout);
			byte[] buf = new byte[512];
			int amount=0;
			while((amount=fis.read(buf))>=0){
				for(int i=0;i<amount;i++){		
					if(buf[i]!=0x20){
						fos.write(buf[i]);
					} else {
						if(i+1 < buf.length && buf[i+1]==0x20){
							fos.write(buf[i+1]);
							
							i+=1;
						}
					}
				}
			}
			
		} catch (Exception e){
			System.out.println("\tERROR HERE IN SPACEREMOVER");
			e.printStackTrace();
		}
		System.out.println("Exiting Space Remover");
		
	}
}
