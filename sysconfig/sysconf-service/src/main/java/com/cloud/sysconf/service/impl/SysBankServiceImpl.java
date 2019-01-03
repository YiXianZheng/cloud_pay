package com.cloud.sysconf.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.SysDictDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.redis.lockutil.DistributedLockHandler;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.utils.page.PageResult;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.dao.SysBankDao;
import com.cloud.sysconf.dao.SysDictDao;
import com.cloud.sysconf.dao.ThirdChannelBankDao;
import com.cloud.sysconf.po.SysBank;
import com.cloud.sysconf.po.SysDict;
import com.cloud.sysconf.service.SysBankService;
import com.cloud.sysconf.service.SysDictService;
import com.cloud.sysuser.provider.SysUserProvider;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
@Service
public class SysBankServiceImpl extends BaseMybatisServiceImpl<SysBank, String, SysBankDao> implements SysBankService {

    @Autowired
    private ThirdChannelBankDao thirdChannelBankDao;
    @Autowired
    private SysBankDao sysBankDao;

    @Override
    public String getByChannelAndSysCode(String sysBankCode, String thirdChannelId){
        return thirdChannelBankDao.getByChannelAndSysCode(sysBankCode, thirdChannelId);
    }

    @Override
    public List<Map<String, String>> getSysSelectList() {
        List<SysBank> list = sysBankDao.getUsable();
        List<Map<String, String>> banks = new ArrayList<>();
        for (SysBank bank: list
             ) {
            Map<String, String> map = new HashMap<>();
            map.put("code", bank.getBankCode());
            map.put("value", bank.getBankName());

            banks.add(map);
        }
        return banks;
    }

    @Override
    public String getBankNameByCode(String bankCode) {
        SysBank sysBank = sysBankDao.getByCode(bankCode);
        if(sysBank != null)
            return sysBank.getBankName();
        else
            return "";
    }

}
