package memory;

import java.io.Serializable;

public class MemoryMDL implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double _ValidCount = 0;
	/** 
	 遗忘累频(词权重)
	*/
	public final double getValidCount()
	{
		return _ValidCount;
	}
	public final void setValidCount(double value)
	{
		_ValidCount = value;
	}
	
	private double _TotalCount = 0;
	/** 
	 累计次数
	*/
	public final double getTotalCount()
	{
		return _TotalCount;
	}
	public final void setTotalCount(double value)
	{
		_TotalCount = value;
	}

	private double _UpdateOffsetCount = 0;
	/** 
	 最后一次更新时的系统总偏移量（用于模拟计时）
	*/
	public final double getUpdateOffsetCount()
	{
		return _UpdateOffsetCount;
	}
	public final void setUpdateOffsetCount(double value)
	{
		_UpdateOffsetCount = value;
	}

	private double _ValidDegree = 1;
	/** 
	 有效程度（成熟度）
	 成熟度的物理含义：成熟的标志是遗忘的量与出现的量基本一致 
	*/
	public final double getValidDegree()
	{
		return _ValidDegree;
	}
	public final void setValidDegree(double value)
	{
		_ValidDegree = value;
	}
}