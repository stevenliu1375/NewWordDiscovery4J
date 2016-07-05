package dictionary;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import memory.MemoryDAL;

public class DictionaryDAL {
	/**
	 * 计算当前关键词的成熟度
	 * 
	 * <typeparam name="T">泛型，具体类别由调用者传入</typeparam>
	 * 
	 * @param mdl
	 *            待计算的对象
	 * @param dRemeberValue
	 *            记忆剩余量系数
	 * @return 当前成熟度
	 * 
	 *         1、成熟度这里用对象遗忘与增加的量的残差累和来表征；
	 *         2、已经累计的残差之和会随时间衰减； 
	 *         3、公式的意思是： 成熟度 = 成熟度衰减剩余量 + 本次遗忘与增加量的残差的绝对值
	 */
	public static <T> double CalcValidDegree(MemoryItemMDL<T> mdl, double dRemeberValue) {
		return mdl.getValidDegree() * dRemeberValue + Math.abs(1 - mdl.getValidCount() * (1 - dRemeberValue));
	}

	/**
	 * 计算候选项记忆剩余量
	 * 
	 * 泛型，具体类型由调用者传入
	 * 
	 * @param key
	 *            候选项
	 * @param objMemoryItemColl
	 *            候选项集合
	 * @return 返回记忆剩余量
	 */
	public static <T> double CalcRemeberValue(T key, MemoryItemColl<T> objMemoryItemColl) {
		if (!objMemoryItemColl.containsKey(key)) {
			return 0;
		}
		MemoryItemMDL<T> mdl = objMemoryItemColl.get(key);
		double dRemeberValue = MemoryDAL.CalcRemeberValue(
				objMemoryItemColl.getOffsetTotalCount() - mdl.getUpdateOffsetCount(),
				objMemoryItemColl.getMinuteOffsetSize());
		return mdl.getValidCount() * dRemeberValue;
	}

	/**
	 * 计算邻键首项记忆剩余量
	 * 
	 * <typeparam name="T">泛型，具体类别由调用者传入</typeparam>
	 * 
	 * @param key
	 *            相邻两项的首项
	 * @param objMemoryBondColl
	 *            邻键集合
	 * @return 返回记忆剩余量
	 */
	public static <T> double CalcRemeberValue(T key, MemoryBondColl<T> objMemoryBondColl) {
		if (!objMemoryBondColl.containsKey(key)) {
			return 0;
		}
		MemoryBondMDL<T> objBondMDL = objMemoryBondColl.get(key);
		MemoryItemMDL<T> mdl = objBondMDL.getKeyItem();
		double dRemeberValue = MemoryDAL.CalcRemeberValue(
				objMemoryBondColl.getOffsetTotalCount() - mdl.getUpdateOffsetCount(),
				objMemoryBondColl.getMinuteOffsetSize());
		return mdl.getValidCount() * dRemeberValue;
	}

	/**
	 * 计算邻键尾项记忆剩余量
	 * 
	 * <typeparam name="T">泛型，具体类别由调用者传入</typeparam>
	 * 
	 * @param keyHead
	 *            相邻两项的首项
	 * @param keyTail
	 *            相邻两项的尾项
	 * @param objMemoryBondColl
	 *            邻键集合
	 * @return 返回记忆剩余量
	 */
	public static <T> double CalcRemeberValue(T keyHead, T keyTail, MemoryBondColl<T> objMemoryBondColl) {
		if (!objMemoryBondColl.containsKey(keyHead)) {
			return 0;
		}
		MemoryBondMDL<T> objBondMDL = objMemoryBondColl.get(keyHead);
		MemoryItemColl<T> objLinkColl = objBondMDL.getLinkColl();
		return CalcRemeberValue(keyTail, objLinkColl);
	}

	/**
	 * 判断键是否为有效关联键
	 * 
	 * <typeparam name="T">泛型，具体类型由调用者传入</typeparam>
	 * 
	 * @param keyHead
	 *            相邻键中首项
	 * @param keyTail
	 *            相邻键中尾项
	 * @param objMemoryBondColl
	 *            相邻字典
	 * @return 返回是否判断的结果：true、相邻项有关；false、相邻项无关 判断标准：共享键概率 > 单字概率之积
	 */
	public static <T> boolean IsBondValid(T keyHead, T keyTail, MemoryBondColl<T> objMemoryBondColl) {
		// 如果相邻项任何一个不在相邻字典中，则返回false 。
		if (!objMemoryBondColl.containsKey(keyHead) || !objMemoryBondColl.containsKey(keyTail)) {
			return false;
		}

		// 分别获得相邻单项的频次
		double dHeadValidCount = CalcRemeberValue(keyHead, objMemoryBondColl);
		double dTailValidCount = CalcRemeberValue(keyTail, objMemoryBondColl);
		// 获得相邻字典全库的总词频
		double dTotalValidCount = objMemoryBondColl.getMinuteOffsetSize();

		if (dTotalValidCount <= 0) {
			return false;
		}

		// 获得相邻项共现的频次
		MemoryItemColl<T> objLinkColl = objMemoryBondColl.get(keyHead).getLinkColl();
		if (!objLinkColl.containsKey(keyTail)) {
			return false;
		}
		double dShareValidCount = CalcRemeberValue(keyTail, objLinkColl);

		// 返回计算的结果
		return dShareValidCount / dHeadValidCount > dTailValidCount / dTotalValidCount;

	}

	/**
	 * 清理邻键集合，移除低于阈值的邻键
	 * 
	 * <typeparam name="T"></typeparam>
	 * 
	 * @param objMemoryBondColl
	 * @param dMinValidValue
	 */
	public static <T> void ClearMemoryBondColl(MemoryBondColl<T> objMemoryBondColl, double dMinValidValue) {
		// 过滤掉词权重小于阈值的词
		// 建立临时map，遍历字典中不可做remove操作，否则会报ConcurrentModificationException
		Map<T, MemoryBondMDL<T>> map = new HashMap<T, MemoryBondMDL<T>>();
		map.putAll(objMemoryBondColl);

		Iterator<Entry<T, MemoryBondMDL<T>>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<T, MemoryBondMDL<T>> entry = it.next();
			if (entry.getValue().getKeyItem().getValidCount() < dMinValidValue) {
				objMemoryBondColl.remove(entry.getKey());
			} else {
				ClearMemoryItemColl(entry.getValue().getLinkColl(), dMinValidValue);
			}
		}

		// for (Entry<T, MemoryItemMDL<T>> entry: objMemoryItemColl.entrySet())
		// {
		// double dValidValue = CalcRemeberValue(entry.getValue().getKey(),
		// objMemoryItemColl);
		// if (dValidValue < dMinValidValue)
		// objMemoryItemColl.remove(entry.getKey());
		// if (entry.getValue().getValidCount() < dMinValidValue) {
		// objMemoryItemColl.remove(entry.getKey());
		// }
		// }
	}

	/**
	 * 将相邻两项（邻键）添加到集合中 <typeparam name="T"></typeparam>
	 * 
	 * @param keyHead
	 * @param keyTail
	 * @param objMemoryBondColl
	 */
	public static <T> void UpdateMemoryBondColl(T keyHead, T keyTail, MemoryBondColl<T> objMemoryBondColl) {
		if (!objMemoryBondColl.containsKey(keyHead)) {
			MemoryBondMDL<T> bond = new MemoryBondMDL<T>();
			bond.getKeyItem().setKey(keyHead);
			bond.getLinkColl().setOffsetTotalCount(0);
			bond.getLinkColl().setMinuteOffsetSize(objMemoryBondColl.getMinuteOffsetSize());
			objMemoryBondColl.put(bond.getKeyItem().getKey(), bond); // lk很有可能出错
		}

		MemoryBondMDL<T> objBondMDL = objMemoryBondColl.get(keyHead);

		MemoryItemMDL<T> mdl = objBondMDL.getKeyItem();
		double dRemeberValue = MemoryDAL.CalcRemeberValue(
				objMemoryBondColl.getOffsetTotalCount() - mdl.getUpdateOffsetCount(),
				objMemoryBondColl.getMinuteOffsetSize());
		mdl.setTotalCount(mdl.getTotalCount() + 1); // 累加总计数
		mdl.setValidDegree(CalcValidDegree(mdl, dRemeberValue)); // 计算成熟度
		mdl.setValidCount(mdl.getValidCount() * dRemeberValue + 1); // 遗忘累频=记忆保留量+1
		mdl.setUpdateOffsetCount(objMemoryBondColl.getOffsetTotalCount()); // 更新时的偏移量

		MemoryItemColl<T> objLinkColl = objBondMDL.getLinkColl();
		objLinkColl.setOffsetTotalCount(objMemoryBondColl.getOffsetTotalCount());
		UpdateMemoryItemColl(keyTail, objLinkColl);

		objMemoryBondColl.setOffsetTotalCount(objMemoryBondColl.getOffsetTotalCount() + 1);
	}

	/**
	 * 清理集合，移除低于阈值的项
	 * 
	 * <typeparam name="T">C#中的泛型，具体类型由调用者传入</typeparam>
	 * 
	 * @param objMemoryItemColl
	 *            词库
	 * @param dMinValidValue
	 *            阈值，默认值相当于至少1个记忆周期时间内，未曾再次出现
	 */
	public static <T> void ClearMemoryItemColl(MemoryItemColl<T> objMemoryItemColl, double dMinValidValue) {

		Map<T, MemoryItemMDL<T>> map = new HashMap<T, MemoryItemMDL<T>>();
		map.putAll(objMemoryItemColl);

		Iterator<Entry<T, MemoryItemMDL<T>>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<T, MemoryItemMDL<T>> entry = iterator.next();
			if (entry.getValue().getValidCount() < dMinValidValue) {
				objMemoryItemColl.remove(entry.getKey());
			}
		}
	}

	/**
	 * 将候选项添加到词典中
	 * 
	 * <typeparam name="T">C#中的泛型，具体类型由调用者传入</typeparam>
	 * 
	 * @param keyItem
	 *            候选项
	 * @param objMemoryItemColl
	 *            候选项词典
	 */
	public static <T> void UpdateMemoryItemColl(T keyItem, MemoryItemColl<T> objMemoryItemColl) {

		if (!objMemoryItemColl.containsKey(keyItem)) {
			// 如果词典中不存在该候选项

			// 声明数据对象，用于存放候选项及其相关数据
			MemoryItemMDL<T> mdl = new MemoryItemMDL<T>();
			mdl.setKey(keyItem); // 候选项
			mdl.setTotalCount(1); // 候选项出现的物理次数
			mdl.setValidCount(1); // 边遗忘边累加共同作用下的有效次数
			mdl.setValidDegree(1); // 该词的默认成熟度
			objMemoryItemColl.put(mdl.getKey(), mdl); // 添加至词典中
		} else {
			// 如果词典中已包含该候选项

			// 从词典中取出该候选项
			MemoryItemMDL<T> mdl = objMemoryItemColl.get(keyItem);
			// 计算从最后一次入库至现在这段时间剩余量系数
			double dRemeberValue = MemoryDAL.CalcRemeberValue(
					objMemoryItemColl.getOffsetTotalCount() - mdl.getUpdateOffsetCount(),
					objMemoryItemColl.getMinuteOffsetSize());
			mdl.setTotalCount(mdl.getTotalCount() + 1); // 累加总计数
			mdl.setValidDegree(CalcValidDegree(mdl, dRemeberValue)); // 计算成熟度
			mdl.setValidCount(mdl.getValidCount() * dRemeberValue + 1); // 遗忘累频=记忆保留量+1
			mdl.setUpdateOffsetCount(objMemoryItemColl.getOffsetTotalCount()); // 更新时的偏移量（相当于记录本次入库的时间）
		}

		objMemoryItemColl.setOffsetTotalCount(objMemoryItemColl.getOffsetTotalCount() + 1); // 处理过的数据总量（相当于一个全局的计时器）
	}
}
