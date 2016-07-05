package main;

import java.util.ArrayList;

import dictionary.*;

/**
 * 该分词代码还没有开发好，可能需要修改
 * @author steven
 *
 */

public class SegmentBLL
{
	public static String ShowSegment(ArrayList<String> buffer)
	{
		StringBuilder sb = new StringBuilder();
		for (String keyword : buffer)
		{
			sb.append(String.format("<%1$s>",keyword));
		}
		return sb.toString();
	}

	/** 
	 分词（同时自动维护词典）
	 
	 @param text 待分词文本
	 @param objCharBondColl 邻键集合（用于生成词库）
	 @param objKeyWordColl 词库
	 @param maxWordLen 最大词长（建议：细粒度为4、粗粒度为7）
	 @param bUpdateCharBondColl 是否同时更新邻键集合
	 @param bUpdateKeyWordColl 是否同时更新词库
	 @return 返回分词结果
	*/

	//ORIGINAL LINE: public static List<string> Segment(string text, MemoryBondColl<string> objCharBondColl, MemoryItemColl<string> objKeyWordColl, int maxWordLen = 7, bool bUpdateCharBondColl = true, bool bUpdateKeyWordColl = true)
	public static ArrayList<String> Segment(String text, MemoryBondColl<String> objCharBondColl, MemoryItemColl<String> objKeyWordColl, int maxWordLen, boolean bUpdateCharBondColl, boolean bUpdateKeyWordColl)
	{
		if (text == null || text.equals(""))
		{
			return new ArrayList<String>();
		}
		if (maxWordLen == 0)
		{
			maxWordLen = text.length();
		}

		//此处使用了个技巧：偶尔发现，词库在遗忘公式作用下，其总量也为相对稳定的固定值，且与MinuteOffsetSize相当。
		//故此处以此替换所有词的遗忘后的总词频，这样可以在处理流式数据时，避免动态计算词库总词频（因其计算量较大）。
		double dLogTotalCount = Math.log(objKeyWordColl.getMinuteOffsetSize());

		if (bUpdateCharBondColl || bUpdateKeyWordColl)
		{
			WordDictBLL.UpdateKeyWordColl(text, objCharBondColl, objKeyWordColl, bUpdateCharBondColl, bUpdateKeyWordColl);
		}

		java.util.HashMap<Integer, ArrayList<String>> objKeyWordBufferDict = new java.util.HashMap<Integer, ArrayList<String>>();
		java.util.HashMap<Integer, Double> objKeyWordValueDict = new java.util.HashMap<Integer, Double>();

		for (int k = 0; k < text.length(); k++)
		{
			ArrayList<String> objKeyWordList = new ArrayList<String>();
			double dKeyWordValue = 0;

			for (int len = 0; len <= Math.min(k, maxWordLen); len++)
			{
				int startpos = k - len;
				String keyword = text.substring(startpos, startpos + len + 1);
				if (len > 0 && !objKeyWordColl.containsKey(keyword))
				{
					continue;
				}
				double dTempValue = 0;
				if (objKeyWordColl.containsKey(keyword))
				{
					dTempValue = -(dLogTotalCount - Math.log(DictionaryDAL.<String>CalcRemeberValue(keyword, objKeyWordColl)));
				}
				if (objKeyWordValueDict.containsKey(startpos - 1))
				{
					dTempValue += objKeyWordValueDict.get(startpos - 1);
					if (dKeyWordValue == 0 || dTempValue > dKeyWordValue)
					{
						dKeyWordValue = dTempValue;
						objKeyWordList = new ArrayList<String>(objKeyWordBufferDict.get(startpos - 1));
						objKeyWordList.add(keyword);
					}
				}
				else
				{
					if (dKeyWordValue == 0 || dTempValue > dKeyWordValue)
					{
						dKeyWordValue = dTempValue;
						objKeyWordList = new ArrayList<String>();
						objKeyWordList.add(keyword);
					}
				}

			}
			objKeyWordBufferDict.put(k, objKeyWordList);
			objKeyWordValueDict.put(k, dKeyWordValue);
		}

		return objKeyWordBufferDict.get(text.length() - 1);
	}

}