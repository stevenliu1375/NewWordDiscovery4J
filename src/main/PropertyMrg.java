package main;

import java.io.IOException;
import java.util.Properties;

public class PropertyMrg {
	
	public static int nKeyWordTopCount = 5000;
	public static int nMinWordSize = 2;
	public static boolean bOrderbyDesc = true;
	public static boolean bIsOnlyWord = true;
	public static double dMinValidValue = 1.254;
	
	public static String filePathIn = null;
	public static String filePathOut = null;
	
	static Properties props; 
	
	static {
		
		try {
			if (props == null) {
				props = new Properties();
				props.load(ClassLoader.getSystemResourceAsStream("config.properties"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		nKeyWordTopCount = Integer.parseInt(props.getProperty("nKeyWordTopCount"));
		nMinWordSize = Integer.parseInt(props.getProperty("nMinWordSize"));
		bOrderbyDesc = Boolean.parseBoolean(props.getProperty("bOrderbyDesc"));
		bIsOnlyWord = Boolean.parseBoolean(props.getProperty("bIsOnlyWord"));
		dMinValidValue = Double.parseDouble(props.getProperty("dMinValidValue"));
		filePathIn = props.getProperty("filePathIn");
		filePathOut = props.getProperty("filePathOut");
	}
	
}
