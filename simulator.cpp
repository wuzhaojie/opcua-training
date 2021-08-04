/**********************模拟生成各种波形数据*****************************/
//执行一次采集类型计算
//当前采集值 dbProfile  计算辅助值nTypeHelp可能会发生改变 nOffsetCount值0
void CSimulatorProfile::Account()
{
    //常量不用算了

    //增量 dbType表示每次递增值
    if (m_SimuProfile.nSimulateType==AccountData_Type_Incremental)
    {
        CAccountData::Incremental(m_SimuProfile.dbProfile, m_SimuProfile.dbMin,
            m_SimuProfile.dbMax, m_SimuProfile.dbType);
    }
    //正弦波 dbType表示一个波形周期的时间 nTypeHelp表示波形周期内的时间
    else if (m_SimuProfile.nSimulateType==AccountData_Type_Sine)
    {
        CAccountData::Sine(m_SimuProfile.dbProfile, m_SimuProfile.dbMin,
            m_SimuProfile.dbMax, m_SimuProfile.dbType, m_SimuProfile.nTypeHelp);
        //周期内的时间 要处理
        m_SimuProfile.nTypeHelp += m_SimuProfile.nPeriod;
        if (m_SimuProfile.nTypeHelp >= m_SimuProfile.dbType)
        {
            m_SimuProfile.nTypeHelp -= (PSUINT32)m_SimuProfile.dbType;
        }
    }
    //三角波 dbType表示每次增加的值 nTypeHelp为1表示增, 0表示减
    else if (m_SimuProfile.nSimulateType==AccountData_Type_Triangle)
    {
        CAccountData::Triangle(m_SimuProfile.dbProfile, m_SimuProfile.dbMin,
            m_SimuProfile.dbMax, m_SimuProfile.dbType, m_SimuProfile.nTypeHelp);
    }
    //方波 dbType表示一个波形周期的时间 nTypeHelp表示波形周期内的时间
    else if (m_SimuProfile.nSimulateType==AccountData_Type_Square)
    {
        m_SimuProfile.nTypeHelp += m_SimuProfile.nPeriod;
        //周期内的时间大于周期的时间时 改变值
        if (m_SimuProfile.nTypeHelp >= m_SimuProfile.dbType)
        {
            m_SimuProfile.nTypeHelp -= (PSUINT32)m_SimuProfile.dbType;
            if (m_SimuProfile.dbProfile==m_SimuProfile.dbMin)
            {
                m_SimuProfile.dbProfile=m_SimuProfile.dbMax;
            }
            else
            {
                m_SimuProfile.dbProfile=m_SimuProfile.dbMin;
            }
        }
    }
    //随机数
    else if (m_SimuProfile.nSimulateType==AccountData_Type_Random)
    {
        CAccountData::Random(m_SimuProfile.dbProfile, m_SimuProfile.dbMin,
            m_SimuProfile.dbMax);
    }
} 


/*************************测点偏移量*******************************/
//每个测点执行一次
//计算一次偏移值 包含随机偏移值和配置内偏移值 得到dbOffsetVar
inline PSDOUBLE CSimulatorProfile::OffsetValue(PSDOUBLE dbProfile, PSUINT32 nOffsetIndex)
{
    PSDOUBLE dbOffsetVar = dbProfile;
    if (m_SimuProfile.dbRandomOffset >= EPSINON || m_SimuProfile.dbRandomOffset <= -EPSINON)
    {
        dbOffsetVar = rand();
        dbOffsetVar = dbOffsetVar/RAND_MAX;
        dbOffsetVar = dbProfile 
            + m_SimuProfile.dbRandomOffset*dbOffsetVar;
    }
    if (m_SimuProfile.dbProfileOffset >= EPSINON || m_SimuProfile.dbProfileOffset <= -EPSINON)
    {
        dbOffsetVar += m_SimuProfile.dbProfileOffset*nOffsetIndex;
    }
    return dbOffsetVar;
}


/***********************单个点执行过程*******************************/
//一次计算并且提交值到服务器
//会执行一次采集类型计算 对每个测点会执行偏移值计算
//批次提交数据到服务器
void CSimulatorProfile::UpdateData()
{
    //如果已经停止 直接返回
    if (!g_SimulatorData.RunState())
    {
        return;
    }
    Account();
    //把当前计算的模拟采集值赋给局部变量
    PSDOUBLE dbProfile = m_SimuProfile.dbProfile;
    PSUINT32 nBatch = 0;
	//计算时间戳
	if (m_SimuProfile.nIncrement == 0)
	{
		m_SimuProfile.psTm = pSpaceCTL::CpsTime::GetCurTime();
	}
	else if (m_SimuProfile.psTm.Second == 0)
	{
		//设置了递增时间戳 第一次运行的情况
		m_SimuProfile.psTm = m_SimuProfile.StartTime;
	}
	else
	{
		pSpaceCTL::CpsTime ct = m_SimuProfile.psTm;
		ct.AddMillisecond(m_SimuProfile.nIncrement);
		m_SimuProfile.psTm = ct;
	}

	//异步写实时
    PSAPIStatus nRet = PSRET_OK;
	PSUINT32 *pTagIds = PSNULL;
	PS_VARIANT realDataList[Batch_Count] = {0};
	PS_TIME realTimeStamps[Batch_Count] = {0};
	PSUINT32 realQualities[Batch_Count] = {0};

    m_LockRunIDList.WaitToRead();
    do 
    {
        //计算批次提交次数
        PSUINT32 nMaxBatch = m_SimuProfile.RunCount/Batch_Count;
        nMaxBatch += (m_SimuProfile.RunCount%Batch_Count)?1:0;
        
        if (nBatch >= nMaxBatch)
        {
            break;
        }
        //判断是否最后一次循环
        PSUINT32 nCount = Batch_Count;
        if((nBatch+1)==nMaxBatch && m_SimuProfile.RunCount%Batch_Count)
        {
            nCount = m_SimuProfile.RunCount%Batch_Count;
        }
        nCount += nBatch*Batch_Count;
        //再循环计算每个测点的值
        for (PSUINT32 n = nBatch*Batch_Count; n < nCount; n++)
        {
            //多线程经常出错的就是这里
            if (m_SimuProfile.RunIDList==NULL)
            {
                ASSERT(FALSE);
                break;
            }
            SimulatorTag* simuTag = CSimulatorTags::SimuTags.GetAt(*(m_SimuProfile.RunIDList+n));
            if (simuTag==NULL)
            {
                ASSERT(FALSE);
                break;
            }
            //偏移后的值 提交的是这个
            PSDOUBLE dbOffsetVar = OffsetValue(dbProfile, n); 

            //转换数据类型
            PS_VARIANT var = {0};
            var.DataType = simuTag->nDateType;
            switch (simuTag->nDateType)
            {
            case PSDATATYPE_BOOL:
                var.Bool = (bool)dbOffsetVar;
                break;
            case PSDATATYPE_INT8:
                var.Int8 = (PSINT8)dbOffsetVar;
            	break;
            case PSDATATYPE_UINT8:
                var.UInt8 = (PSUINT8)dbOffsetVar;
                break;
            case PSDATATYPE_INT16:
                var.Int16 = (PSINT16)dbOffsetVar;
                break;
            case PSDATATYPE_UINT16:
                var.UInt16 = (PSUINT16)dbOffsetVar;
                break;
            case PSDATATYPE_INT32:
                var.Int32 = (PSINT32)dbOffsetVar;
                break;
            case PSDATATYPE_UINT32:
                var.UInt32 = (PSUINT32)dbOffsetVar;
                break;
            case PSDATATYPE_INT64:
                var.Int64 = (PSINT64)dbOffsetVar;
                break;
            case PSDATATYPE_UINT64:
                var.UInt64 = (PSUINT64)dbOffsetVar;
                break;
            case PSDATATYPE_FLOAT:
                var.Float = (PSFLOAT)dbOffsetVar;
                break;
            case PSDATATYPE_DOUBLE:
                var.Double = (PSDOUBLE)dbOffsetVar;
                break;
            }
            PSUINT32 nIndex = n-nBatch*Batch_Count;
			realDataList[nIndex] = var;
			realTimeStamps[nIndex] = m_SimuProfile.psTm;
			realQualities[nIndex] = m_SimuProfile.nQuality;
            //计算出来的值保存到SimulatorTag
            simuTag->Value = var;
            //在界面上显示值
            if (g_pShowChart != NULL && 
                    g_pShowChart->GetTagID()== simuTag->nID)
            {
                //如果是开关量
                g_pShowChart->PushValue(m_SimuProfile.psTm, PS_VARIANT2DOUBLE(var));
            }
        }
        //批量异步提交
		pTagIds = m_SimuProfile.RunIDList+nBatch*Batch_Count;
		//nRet = psAPI_Real_WriteListAsyn(g_hServer, nCount - nBatch*Batch_Count,
		//	pTagIds, realDataList, realTimeStamps, realQualities, Real_WriteListAsynComplete, NULL);	
		//if (PSERR(nRet) && g_pShowChart != NULL)
		//      {
		//          g_pShowChart->WriteListError(nRet);
		//      }

		//改成同步提交
		PSAPIStatus *pAPIErrors = PSNULL;
		nRet = psAPI_Real_WriteList(g_hServer, nCount - nBatch*Batch_Count,
				pTagIds, realDataList, realTimeStamps, realQualities, &pAPIErrors);
		if (PSERR(nRet))
		{
			if (nRet == PSERR_FAIL_IN_BATCH)
			{
				for (int n = 0; n < nCount - nBatch*Batch_Count; n++)
				{
					if (PSERR(pAPIErrors[n]) && (pAPIErrors[n] != PSERR_HIS_TIMESTAMP_EARLY))
					{
						ACE_DEBUG((LM_ERROR, "%D 模拟采集器写实时出错了 测点ID:%u %s\n", 
							pTagIds[n], psAPI_Commom_GetErrorDesc(pAPIErrors[n])));
						if (g_pShowChart != NULL)
						{
							g_pShowChart->WriteListError(pAPIErrors[n]);
						}
						break;
					}
				}
				psAPI_Memory_FreeAndNull((PSVOID**)&pAPIErrors);
			}
			else
			{
				ACE_DEBUG((LM_ERROR, "%D 模拟采集器psAPI_Real_WriteList写实时出错了 %s\n", psAPI_Commom_GetErrorDesc(nRet)));
				if (g_pShowChart != NULL)
				{
					g_pShowChart->WriteListError(nRet);
				}
			}
		}
        nBatch++;
    } while (true);
    m_LockRunIDList.Done();

}