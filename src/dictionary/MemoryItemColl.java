package dictionary;

import java.io.Serializable;
import java.util.HashMap;

public class MemoryItemColl<T> extends HashMap<T, MemoryItemMDL<T>> implements Serializable
{
	private static final long serialVersionUID = 1L;
	private double _OffsetTotalCount = 0;
	/** 
	 偏移总量
	*/
	public final double getOffsetTotalCount()
	{
		return _OffsetTotalCount;
	}
	public final void setOffsetTotalCount(double value)
	{
		_OffsetTotalCount = value;
	}

	private double _MinuteOffsetSize = 7 * 60 * 60 * 24 * 6; //最大记忆容量 - 一天 - 每秒7个字符
	/** 
	 每分钟偏移量
	*/
	public final double getMinuteOffsetSize()
	{
		return _MinuteOffsetSize;
	}
	public final void setMinuteOffsetSize(double value)
	{
		_MinuteOffsetSize = value;
	}

	public T GetKeyForItem(MemoryItemMDL<T> item)
	{
		return item.getKey();
	}
}