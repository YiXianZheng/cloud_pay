package com.cloud.sysconf.service.impl;

import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.SysMenuDto;
import com.cloud.sysconf.common.dto.SysOfficeDto;
import com.cloud.sysconf.common.dto.SysRoleDto;
import com.cloud.sysconf.common.utils.BuildTree;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.dao.SysOfficeDao;
import com.cloud.sysconf.po.SysMenu;
import com.cloud.sysconf.po.SysOffice;
import com.cloud.sysconf.po.SysRole;
import com.cloud.sysconf.po.SysRoleMenu;
import com.cloud.sysconf.service.SysOfficeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
@Service
public class SysOfficeServiceImpl extends BaseMybatisServiceImpl<SysOffice, String, SysOfficeDao> implements SysOfficeService {

    @Autowired
    private SysOfficeDao sysOfficeDao;

    @Override
    public ReturnVo getBySysUser(String curUserId) {
        try {
            List<SysOffice> list = sysOfficeDao.querySysOffice();
            List<SysOfficeDto> offices = new ArrayList<>();
            for (SysOffice sysOffice : list) {
                SysOfficeDto officeDto = new SysOfficeDto();
                BeanUtils.copyProperties(sysOffice, officeDto);

                offices.add(officeDto);
            }
            return ReturnVo.returnSuccess(ResponseCode.Base.SUCCESS, BuildTree.buildOffice(offices));
        }catch (Exception e){
            e.printStackTrace();
            return ReturnVo.returnFail();
        }
    }

    @Override
    public ReturnVo saveOrUpdate(SysOfficeDto sysOfficeDto, HeaderInfoDto headerInfoDto) {
        try{
            if(sysOfficeDto == null) return ReturnVo.returnFail();
            if(StringUtils.isBlank(sysOfficeDto.getParentId())) sysOfficeDto.setParentId("0");

            SysOffice sysOffice = null;
            if(StringUtils.isNotBlank(sysOfficeDto.getId())){
                sysOffice = sysOfficeDao.getById(sysOfficeDto.getId());
            }else{
                sysOffice = new SysOffice();
            }

            if(!"0".equals(sysOfficeDto.getParentId())){
                if(SysOffice.TYPE_COMPANY == sysOfficeDto.getType()){
                    return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.API_ERR.getCode(),
                            "公司必须放在第一层级"));
                }
                SysOffice parent = sysOfficeDao.getById(sysOfficeDto.getParentId());
                if(!"0".equals(parent.getParentId())){
                    return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.API_ERR.getCode(),
                            "层级关系超过限定数"));
                }
                if(!SysOffice.DEL_FLAG_COMMON.equals(parent.getDelFlag())){
                    return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.API_ERR.getCode(),
                            "所属公司不存在或已删除"));
                }
            }else{
                if(SysOffice.TYPE_DEPARTMENT == sysOfficeDto.getType()){
                    return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.API_ERR.getCode(),
                            "部门必须下属某一个公司"));
                }
            }

            if((StringUtils.isNotBlank(sysOffice.getId()) && (sysOfficeDao.checkExist(sysOfficeDto.getName(), sysOfficeDto.getType())>1))
                    || (StringUtils.isBlank(sysOffice.getId()) && (sysOfficeDao.checkExist(sysOfficeDto.getName(), sysOfficeDto.getType())>0))
                    ){
                String temp = "";
                if(SysOffice.TYPE_COMPANY == sysOfficeDto.getType()){
                    temp = "公司";
                }else if(SysOffice.TYPE_DEPARTMENT == sysOfficeDto.getType()){
                    temp = "部门";
                }
                return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.API_ERR.getCode(),
                        temp + "名称重复"));
            }


            if(StringUtils.isBlank(sysOffice.getId())){
                BeanUtils.copyProperties(sysOfficeDto, sysOffice);

                sysOffice.preInsert(headerInfoDto.getCurUserId(), headerInfoDto.getPanId());
                sysOfficeDao.add(sysOffice);
            }else{
                sysOffice.setParentId(sysOfficeDto.getParentId());
                sysOffice.setName(sysOfficeDto.getName());
                sysOffice.setType(sysOfficeDto.getType());
                sysOffice.setAddress(sysOfficeDto.getAddress());
                sysOffice.setMaster(sysOfficeDto.getMaster());
                sysOffice.setPhone(sysOfficeDto.getPhone());
                sysOffice.setFax(sysOfficeDto.getFax());
                sysOffice.setEmail(sysOfficeDto.getEmail());
                sysOffice.setUsable(sysOfficeDto.getUsable());
                sysOffice.preUpdate(headerInfoDto.getCurUserId());

                sysOfficeDao.update(sysOffice);
            }

            return ReturnVo.returnSuccess();
        }catch (Exception e){
            e.printStackTrace();
            return ReturnVo.returnError();
        }
    }

    @Override
    public ReturnVo getDetail(String id) {
        try{
            SysOffice sysOffice = sysOfficeDao.getById(id);
            if(sysOffice == null || SysOffice.DEL_FLAG_ALREADY == sysOffice.getDelFlag())
                return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "组织机构不存在或已删除"));

            SysOfficeDto sysOfficeDto = new SysOfficeDto();
            BeanUtils.copyProperties(sysOffice, sysOfficeDto);

            return ReturnVo.returnSuccess(sysOfficeDto);
        }catch (Exception e){
            e.printStackTrace();
            return ReturnVo.returnError();
        }
    }

    @Override
    public ReturnVo getAllUsable() {
        try {
            List<SysOffice> list = sysOfficeDao.querySysOffice();
            List<SysOfficeDto> offices = new ArrayList<>();
            for (SysOffice sysOffice : list) {
                SysOfficeDto officeDto = new SysOfficeDto();
                BeanUtils.copyProperties(sysOffice, officeDto);

                offices.add(officeDto);
            }
            return ReturnVo.returnSuccess(ResponseCode.Base.SUCCESS, BuildTree.buildOfficeMap(offices));
        }catch (Exception e){
            e.printStackTrace();
            return ReturnVo.returnFail();
        }
    }
}
