package bpe;

public class Main {

	public static void main(String[] args) {
		String FileName = "poolcheck.drawio.xml";
		 XPDLParser parser = new  XPDLParser();
		 parser.parseBPMN(FileName);
	}

}
