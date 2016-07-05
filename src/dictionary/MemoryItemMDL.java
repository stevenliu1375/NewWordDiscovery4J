package dictionary;

import java.io.Serializable;

import memory.*;

/**
 * 词库 MemoryItemMDL
 * @author steven
 * @param <T>
 */
public class MemoryItemMDL<T> extends MemoryMDL implements Serializable
{
	private static final long serialVersionUID = 1L;
	private T _Key = null;
	/** 
	 记忆单元主体项(生成的词)
	*/
	public final T getKey()
	{
		return _Key;
	}
	public final void setKey(T value)
	{
		_Key = value;
	}
}