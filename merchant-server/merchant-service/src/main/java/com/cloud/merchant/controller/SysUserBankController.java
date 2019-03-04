package com.cloud.merchant.controller;

import com.cloud.merchant.common.dto.SysUserBankDto;
import com.cloud.merchant.dao.SysUserBankDao;
import com.cloud.merchant.po.SysUserBank;
import com.cloud.merchant.service.SysUserBankService;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/merchant/bank")
public class SysUserBankController extends BaseController {

    @Autowired
    private SysUserBankService sysUserBankService;
    @Autowired
    private SysUserBankDao sysUserBankDao;

    /**
     * 添加银行卡
     * @param sysUserBankDto
     * @param headers
     * @return
     */
    @PostMapping("/addBankCard")
    public ApiResponse addBankCard(@RequestBody SysUserBankDto sysUserBankDto, @RequestHeader HttpHeaders headers) {

        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);

        try {
            ReturnVo returnVo = sysUserBankService.addBankCard(sysUserBankDto, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 根据用户编号分页搜索银行卡  适用于列表分页
     * @param headers
     * @return
     */
    @PostMapping("/listPage")
    public ApiResponse getByUserId(@RequestBody PageQuery pageQuery, @RequestHeader HttpHeaders headers) {

        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            Map<String, Object> params = pageQuery.getParams();
            if(params == null)
                params = new HashMap<>();
            if (HeaderInfoDto.AUTH_MERCHANT_SYSTEM.equals(headerInfoDto.getAuth())) {
                // 商户端查询的是自己的银行卡列表
                params.put("sysUserId", headerInfoDto.getCurUserId());
                // 先处理下发数据
                sysUserBankService.summaryPaid(headerInfoDto.getCurUserId());
            }
            pageQuery.setParams(params);
            // 银行卡号
            return this.toApiResponse(sysUserBankService.listForTablePage(pageQuery, headerInfoDto));
        } catch (Exception e){
            e.printStackTrace();
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 通过银行卡号获取信息
     * @param bankCardNo
     * @return
     */
    @PostMapping("/getByCardNo")
    public ApiResponse getByCardNo(@RequestParam("bankCardNo") String bankCardNo, @RequestHeader HttpHeaders headers) {

        HeaderInfoDto headerInfoDto = getHeaderInfo(headers);
        try {
            if (!HeaderInfoDto.AUTH_MERCHANT_SYSTEM.equals(headerInfoDto.getAuth())) {
                return toApiResponse(ReturnVo.returnFail(ResponseCode.Base.AUTH_ERR));
            }
            ReturnVo returnVo = sysUserBankService.getByCardNo(headerInfoDto.getCurUserId(), bankCardNo);
            return toApiResponse(returnVo);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 审核银行卡（修改银行卡状态）
     * @param id
     * @param headers
     * @return
     */
    @PostMapping("/auth")
    public ApiResponse auth(@RequestParam("id") String id, @RequestHeader HttpHeaders headers) {

        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        if (!HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())) {
            return this.toApiResponse(ReturnVo.returnFail(ResponseCode.Base.AUTH_ERR));
        }

        try {
            // 查看是否为非审核状态
            SysUserBank sysUserBank = sysUserBankDao.getById(id);
            if (sysUserBank == null) {
                logger.info("【银行卡审核】找不到银行卡");
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "找不到银行卡")));
            }
            if (sysUserBank.getCardStatus() != 0) {
                logger.error("【银行卡审核】当前状态不可操作");
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "当前状态不可操作")));
            }

            sysUserBank.setCardStatus(1);
            sysUserBankDao.updateInfo(sysUserBank);

            // 通过审核
            return toApiResponse(ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(), "审核成功")));
        } catch (Exception e) {
            e.printStackTrace();
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "审核失败")));
        }
    }

    /**
     * 修改银行卡信息
     * @param sysUserBankDto
     * @param headers
     * @return
     */
    @PostMapping("/updateInfo")
    public ApiResponse updateInfo(@RequestBody SysUserBankDto sysUserBankDto, @RequestHeader HttpHeaders headers) {

        try {
            HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
            SysUserBank sysUserBank = sysUserBankDao.getById(sysUserBankDto.getId());
            if (sysUserBank == null) {
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "银行卡不存在")));
            }
            // UpdateBankCard
            sysUserBank.setBankBranchName(sysUserBankDto.getBankBranchName());
            sysUserBank.setBankCardNo(sysUserBankDto.getBankCardNo());
            sysUserBank.setBankCardHolder(sysUserBankDto.getBankCardHolder());
            sysUserBank.setBankProvince(sysUserBankDto.getBankProvince());
            sysUserBank.setBankCity(sysUserBankDto.getBankCity());
            sysUserBank.setUpdateBy(headerInfoDto.getCurUserId());
            sysUserBankDao.updateInfo(sysUserBank);
            // 通过审核
            return toApiResponse(ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(), "修改成功")));
        } catch (Exception e) {
            e.printStackTrace();
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "修改失败")));
        }
    }

    /**
     * 删除银行卡
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public ApiResponse delete(@RequestParam("id") String id, @RequestHeader HttpHeaders headers) {

        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try {
            SysUserBank sysUserBank = sysUserBankDao.getById(id);
            if (sysUserBank == null) {
                logger.info("【银行卡删除】找不到银行卡");
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "找不到银行卡")));
            }
            if (sysUserBank.getDelFlag().equals("1")) {
                logger.info("【银行卡删除】当前状态不可操作");
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "当前状态不可操作")));
            }
            sysUserBank.setUpdateBy(headerInfoDto.getCurUserId());
            sysUserBank.setDelFlag("1");
            sysUserBankDao.updateInfo(sysUserBank);
            return toApiResponse(ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(), "删除成功")));
        } catch (Exception e) {
            e.printStackTrace();
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "删除失败")));
        }
    }
}
