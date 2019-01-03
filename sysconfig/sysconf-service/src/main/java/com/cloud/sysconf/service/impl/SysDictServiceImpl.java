package com.cloud.sysconf.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.SysDictDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.redis.lockutil.DistributedLockHandler;
import com.cloud.sysconf.common.redis.lockutil.Lock;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.utils.page.PageResult;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.dao.SysDictDao;
import com.cloud.sysconf.po.SysDict;
import com.cloud.sysconf.service.SysDictService;
import com.cloud.sysuser.provider.SysUserProvider;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
@Service
public class SysDictServiceImpl extends BaseMybatisServiceImpl<SysDict, String, SysDictDao> implements SysDictService {

    @Autowired
    private DistributedLockHandler distributedLockHandler;
    @Autowired
    private SysUserProvider sysUserProvider;
    @Autowired
    private SysDictDao sysDictDao;
    @Autowired
    private RedisClient redisClient;

    @Override
    public ReturnVo findByCode(String code) {
        ReturnVo returnVo = new ReturnVo();
        SysDict sysDict = sysDictDao.findByCode(code);
        if(sysDict != null) {
            SysDictDto sysDictDto = new SysDictDto();
            BeanUtils.copyProperties(sysDict, sysDictDto);
            returnVo.code = ReturnVo.SUCCESS;
            returnVo.object = JSONObject.toJSON(sysDictDto);
        }
        return returnVo;
    }

    @Override
    @PostConstruct //仅在项目启动后运行一次
    public ReturnVo refreshRedis() {
        try {
            logger.info("==============  begin init dict to redis  ===============");
            List<SysDict> list = sysDictDao.findAll();
            for (SysDict dict : list
                    ) {
                redisClient.SetHsetJedis(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, dict.getCode(), dict.getValue());
            }
            logger.info("==============  success init dict to redis  ===============");
            return ReturnVo.returnSuccess();
        }catch (Exception e){
            logger.info("==============  fail to init dict to redis  ===============");
            return ReturnVo.returnFail();
        }
    }

    @Override
    public ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto) {

        try {
            PageResult pageResult = this.queryForTablePage(pageQuery.getPageIndex(), pageQuery.getPageSize(), pageQuery.getParams());
            List<SysDictDto> dictList = initSysDictInfo(pageResult.getData());

            pageResult.setData(dictList);
            return ReturnVo.returnSuccess(pageResult);
        }catch (Exception e){
            return ReturnVo.returnError();
        }
    }

    private List<SysDictDto> initSysDictInfo(List<SysDict> dictList){
        List<SysDictDto> sysDictDtoList = new ArrayList<>();
        for (SysDict dict : dictList) {
            SysDictDto sysDictDto = new SysDictDto();
            BeanUtils.copyProperties(dict, sysDictDto);

            sysDictDtoList.add(sysDictDto);
        }
        return sysDictDtoList;
    }

    @Override
    @Transactional
    public ReturnVo saveOrUpdate(SysDictDto sysDictDto, HeaderInfoDto headerInfoDto) {
        try{
            if(StringUtils.isBlank(sysDictDto.getCode())){
                return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),
                        "字典编码不能为空"));
            }
            if(StringUtils.isBlank(sysDictDto.getValue())){
                return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),
                        "字典值不能为空"));
            }
            SysDict sysDict = sysDictDao.findByCode(sysDictDto.getCode());

            if(SysDictDto.IS_ADD_YES == sysDictDto.getIsAdd()) {
                if(sysDict != null){
                    return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),
                            "字典编码已存在"));
                }
                sysDict = new SysDict();
                BeanUtils.copyProperties(sysDictDto, sysDict);
                sysDict.preInsert(headerInfoDto.getCurUserId(), headerInfoDto.getPanId());
                sysDict.setType("common_value");
                sysDictDao.add(sysDict);
            }else{
                sysDict.setCode(sysDictDto.getCode());
                sysDict.setDescription(sysDictDto.getDescription());
                sysDict.setLabel(sysDictDto.getLabel());
                sysDict.setValue(sysDictDto.getValue());
                sysDict.setSort(sysDictDto.getSort());
                sysDictDao.update(sysDict);
            }
            return ReturnVo.returnSuccess();
        }catch (Exception e){
            e.printStackTrace();

            throw new RuntimeException();
        }
    }

    @Override
    public ReturnVo detail(String code) {
        SysDict sysDict = sysDictDao.findByCode(code);
        SysDictDto sysDictDto = new SysDictDto();
        BeanUtils.copyProperties(sysDict, sysDictDto);
        return ReturnVo.returnSuccess(sysDictDto);
    }

}
