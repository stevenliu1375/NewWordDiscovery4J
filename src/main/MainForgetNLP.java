package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import dictionary.DictionaryDAL;
import dictionary.MemoryBondColl;
import dictionary.MemoryItemColl;
import dictionary.WordDictBLL;

public class MainForgetNLP {

	public static void main(String[] args) throws IOException {
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateStart = new Date();
		System.out.println(df.format(dateStart) + " : running start...");

		keywordWriter(CatchWordIndexColl(PropertyMrg.filePathIn), PropertyMrg.filePathOut);
		
		Date dateEnd = new Date();
		System.out.println(df.format(dateEnd) + " : running ended.");
		// 毫秒差
		long timeConsuming = dateEnd.getTime() - dateStart.getTime();
		System.out.printf("time consuming: %ss", timeConsuming/1000);
	}
	
	public static void keywordWriter(String str, String filePathOut) {
		BufferedWriter bw = null;
		
		try {
			bw = new BufferedWriter(new FileWriter(filePathOut));
			bw.write(str);
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("\nKeywords Created Successfully!\n");
	}
		
	private static String CatchWordIndexColl(String filePathIn) throws IOException {
		
		MemoryBondColl<String> objCharBondColl = new MemoryBondColl<String>();
		MemoryItemColl<String> objKeyWordColl = new MemoryItemColl<String>();
		
		double dTempCharCount = objCharBondColl.getOffsetTotalCount(); 
		BufferedReader br = new BufferedReader(new FileReader(filePathIn));
		
		String line = null;
		String text = null;
		while ((line = br.readLine()) != null) {
			
//			if (!String.IsNullOrWhiteSpace(line)) { //去除可能的控制符、Html标签
//				line = Regex.Replace(Regex.Replace(line, "\\p{C}+", ""), "<.*?>", "", RegexOptions.IgnoreCase | RegexOptions.Singleline);
//			}
			
			if (line.equals("")) continue;

			dTempCharCount += line.length();

//			text = line; //这里可以再做一些需要特别处理的数据清洗，如多余的空格等
			text = Pattern.compile("\\d+、问题分析：").matcher(line).replaceAll("");

			if (!text.equals("")) {
				//当数据跑过一个周期的数据时清理一次邻键集、词库，避免内存空间不足
				if (dTempCharCount > objCharBondColl.getMinuteOffsetSize()) {
					DictionaryDAL.<String>ClearMemoryBondColl(objCharBondColl, PropertyMrg.dMinValidValue);
					DictionaryDAL.<String>ClearMemoryItemColl(objKeyWordColl, PropertyMrg.dMinValidValue);
					dTempCharCount = 0;
				}
				WordDictBLL.UpdateKeyWordColl(line, objCharBondColl, objKeyWordColl, true, true);
			}
			
		}
		
		br.close();
		
		return WordDictBLL.ShowKeyWordWeightColl(objKeyWordColl, PropertyMrg.nKeyWordTopCount,
				PropertyMrg.bOrderbyDesc, PropertyMrg.bIsOnlyWord);
	}
	
}
