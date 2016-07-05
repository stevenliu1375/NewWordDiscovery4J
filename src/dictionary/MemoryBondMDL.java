package dictionary;

import java.io.Serializable;

public class MemoryBondMDL<T> implements Serializable
{
	private static final long serialVersionUID = 1L;
	private MemoryItemMDL<T> _KeyItem = new MemoryItemMDL<T>();
	/** 
	 主项
	*/
	public final MemoryItemMDL<T> getKeyItem()
	{
		return _KeyItem;
	}
	public final void setKeyItem(MemoryItemMDL<T> value)
	{
		_KeyItem = value;
	}

	private MemoryItemColl<T> _LinkColl = new MemoryItemColl<T>();
	/** 
	 关联项集合
	*/
	public final MemoryItemColl<T> getLinkColl()
	{
		return _LinkColl;
	}
	public final void setLinkColl(MemoryItemColl<T> value)
	{
		_LinkColl = value;
	}
}