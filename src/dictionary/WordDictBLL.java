package dictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.regex.Pattern;

import main.PropertyMrg;

public class WordDictBLL
{

	/** 
	 从文本中生成候选词
	 
	 @param text 文本行
	 @param objCharBondColl 相邻字典
	 @param objKeyWordColl 词库
	 @param bUpdateCharBondColl 是否更新相邻字典
	 @param bUpdateKeyWordColl 是否更新词库
	*/

	public static void UpdateKeyWordColl(String text, MemoryBondColl<String> objCharBondColl, MemoryItemColl<String> objKeyWordColl, boolean bUpdateCharBondColl, boolean bUpdateKeyWordColl)
	{
		if (text.equals("") || text == null) return;

		StringBuilder buffer = new StringBuilder(); //用于存放连续的子串
		String keyHead = String.valueOf(text.charAt(0)); //keyHead、keyTail分别存放相邻的两个字符
		buffer.append(keyHead);
		for (int k = 1; k < text.length(); k++) //遍历句子中的每一个字符
		{

			//从句子中取一个字作为相邻两字的尾字
			String keyTail = String.valueOf(text.charAt(k));
			if (bUpdateCharBondColl)
			{
				//更新相邻字典
				DictionaryDAL.<String>UpdateMemoryBondColl(keyHead, keyTail, objCharBondColl);
			}
			if (bUpdateKeyWordColl)
			{
				//判断相邻两字是否有关
				if (!DictionaryDAL.<String>IsBondValid(keyHead, keyTail, objCharBondColl))
				{
					//两字无关，则将绥中的字串取出，此即为候选词
					String keyword = buffer.toString();
					//将候选词添加到词库中
					DictionaryDAL.<String>UpdateMemoryItemColl(keyword, objKeyWordColl);
					//清空缓冲
					buffer.delete(0, buffer.length());
					//并开始下一个子串
					buffer.append(keyTail);
				}
				else
				{
					//两个字有关，则将当前字追加至串缓冲中
					buffer.append(keyTail);
				}
			}
			//将当前的字作为相邻的首字
			keyHead = keyTail;
		}
	}

	/** 
	 相邻字统计
	 
	 @param text 文本行
	 @param objCharBondColl 存放相邻结果的字典
	 
	 遍历句中相邻的字，将结果存放到字典中
	*/
	public static void UpdateCharBondColl(String text, MemoryBondColl<String> objCharBondColl)
	{
		if (text.equals("") || text == null) return;
		
		String keyHead = String.valueOf(text.charAt(0));
		for (int k = 1; k < text.length(); k++)
		{
			String keyTail = String.valueOf(text.charAt(k));
			//存入相邻字典中
			DictionaryDAL.<String>UpdateMemoryBondColl(keyHead, keyTail, objCharBondColl);
			keyHead = keyTail;
		}
	}


	/** 
	 按权重排序输出词库
	 
	 @param objMemoryItemColl 词库
	 @param nKeyWordTopCount 输出词的数量  
	 @param bOrderbyDesc 是否倒序
	 @param bIsOnlyWord 是否仅输出词
	 @return 输出的结果
	*/
	public static String ShowKeyWordWeightColl(MemoryItemColl<String> objMemoryItemColl, int nKeyWordTopCount, boolean bOrderbyDesc, boolean bIsOnlyWord)
	{
		// 计算词库成熟度
		double TotalVaildDegree = 0;
		double dTotalVaildDegree = 0;
		for (MemoryItemMDL<String> memoryItemMDL: objMemoryItemColl.values()) {
			TotalVaildDegree += memoryItemMDL.getValidDegree() / memoryItemMDL.getValidCount();
		}
		dTotalVaildDegree = TotalVaildDegree / objMemoryItemColl.size();
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("词库成熟度：%1$s%% ；", dTotalVaildDegree > 1 ? 0 : String.format("%.2f", ((1 - dTotalVaildDegree) * 100))));
		sb.append("\n----------------------------------------\n");
		sb.append(String.format(" 【%1$s】 | %2$s | %3$s | %4$s | %5$s", "主词", "遗忘词频", "累计词频", "词权值","成熟度(%)"));
		sb.append("\n");
		
		//获取词库中的有效词
		LinkedList<MemoryItemMDL<String>> tbuffer = new LinkedList<MemoryItemMDL<String>>();
		//如果只显示词，则要求：长度大于1、不包含符号、不是纯数字
		for (MemoryItemMDL<String> memoryItemMDL: objMemoryItemColl.values()) {
			if (bIsOnlyWord) {
				if (memoryItemMDL.getKey().length() >= PropertyMrg.nMinWordSize
						&& !Pattern.compile("[\\p{Punct}\\s，。、；：？！【】]").matcher(memoryItemMDL.getKey()).find() 
//						&& !Pattern.compile("[\\p{Punct}\\s]").matcher(memoryItemMDL.getKey()).find() 
						&& !memoryItemMDL.getKey().matches("^\\d+$")) {
					tbuffer.add(memoryItemMDL);
				} 
			} else {
				tbuffer.add(memoryItemMDL);
			}
		}
		
		//按权重排序，判断是否按权重倒排输出
		if (bOrderbyDesc) {
			Collections.sort(tbuffer, Collections.reverseOrder(new ValidCountComparator()));
		} else {
			Collections.sort(tbuffer, new ValidCountComparator());
		}
		
		// 获取词库前n个词的信息
		ArrayList<MemoryItemMDL<String>> buffer = new ArrayList<MemoryItemMDL<String>>();
		if (nKeyWordTopCount == 0) nKeyWordTopCount = Integer.MAX_VALUE;
		for (int i = 0; i < tbuffer.size(); i++) {  
			if (i < nKeyWordTopCount) {
				buffer.add(tbuffer.get(i));
			} 
		}
		
		sb.append(String.format("  ================ 共 %1$s 个 ================", tbuffer.size()));
		sb.append('\n');
		
		//逐词输出，每个词一行
		for (MemoryItemMDL<String> memoryItemMDL : buffer) {
			sb.append(String.format(" 【%1$s】 | %2$s | %3$s | %4$s | %5$s", 
					memoryItemMDL.getKey(),
					String.format("%.2f", DictionaryDAL.<String>CalcRemeberValue(memoryItemMDL.getKey(), objMemoryItemColl)),
					Math.round(memoryItemMDL.getTotalCount()),
					String.format("%.4f", (memoryItemMDL.getValidCount() <= 0 ? 0
							: (memoryItemMDL.getValidCount()) * (Math.log(objMemoryItemColl.getMinuteOffsetSize())
									- Math.log(memoryItemMDL.getValidCount())))),
					memoryItemMDL.getValidCount() <= 1 ? 0
							: memoryItemMDL.getValidDegree() / memoryItemMDL.getValidCount() > 1 ? 0
									: String.format("%.2f", (1 - memoryItemMDL.getValidDegree() / memoryItemMDL.getValidCount()) * 100)));
			sb.append("\n");
		}
		
		return sb.toString();
	}

}


// 自定义比较方法，按词权重
class ValidCountComparator implements Comparator<MemoryItemMDL<String>>  {
	@Override
	public int compare(MemoryItemMDL<String> o1, MemoryItemMDL<String> o2) {
		return new Double(o1.getValidCount()).compareTo(new Double(o2.getValidCount()));
	}
}

//		// 自定义集合元素比较方法
//		Collections.sort(tbuffer, new Comparator<MemoryItemMDL<String>>() {
//			@Override
//			public int compare(MemoryItemMDL<String> o1, MemoryItemMDL<String> o2) {
//				return new Double(o1.getValidCount()).compareTo(new Double(o2.getValidCount()));
//			}
//		});