package com.example.hxds.bff.driver.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import com.example.hxds.bff.driver.controller.form.CreateDriverFaceModelForm;
import com.example.hxds.bff.driver.controller.form.LoginForm;
import com.example.hxds.bff.driver.controller.form.RegisterNewDriverForm;
import com.example.hxds.bff.driver.controller.form.SearchDriverAuthForm;
import com.example.hxds.bff.driver.controller.form.SearchDriverBaseInfoForm;
import com.example.hxds.bff.driver.controller.form.SearchDriverSettingsForm;
import com.example.hxds.bff.driver.controller.form.SearchDriverTodayBusinessDataForm;
import com.example.hxds.bff.driver.controller.form.UpdateDriverAuthForm;
import com.example.hxds.bff.driver.feign.DrServiceApi;
import com.example.hxds.bff.driver.feign.OdrServiceApi;
import com.example.hxds.bff.driver.service.DriverService;
import com.example.hxds.common.util.CosUtil;
import com.example.hxds.common.util.R;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
public class DriverServiceImpl implements DriverService {

    @Resource
    private DrServiceApi drServiceApi;

    @Resource
    private CosUtil cosUtil;

    @Override
    public HashMap searchDriverAuth(SearchDriverAuthForm form) {
        R r = drServiceApi.searchDriverAuth(form);
        HashMap map = (HashMap) r.get("result");
        String idcardFront = MapUtil.getStr(map, "idcardFront");
        String idcardBack = MapUtil.getStr(map, "idcardBack");
        String idcardHolding = MapUtil.getStr(map, "idcardHolding");
        String drcardFront = MapUtil.getStr(map, "drcardFront");
        String drcardBack = MapUtil.getStr(map, "drcardBack");
        String drcardHolding = MapUtil.getStr(map, "drcardHolding");

        String idcardFrontUrl = idcardFront.length() > 0 ? cosUtil.getPrivateFileUrl(idcardFront) : "";
        String idcardBackUrl = idcardBack.length() > 0 ? cosUtil.getPrivateFileUrl(idcardBack) : "";
        String idcardHoldingUrl = idcardHolding.length() > 0 ? cosUtil.getPrivateFileUrl(idcardHolding) : "";
        String drcardFrontUrl = drcardFront.length() > 0 ? cosUtil.getPrivateFileUrl(drcardFront) : "";
        String drcardBackUrl = drcardBack.length() > 0 ? cosUtil.getPrivateFileUrl(drcardBack) : "";
        String drcardHoldingUrl = drcardHolding.length() > 0 ? cosUtil.getPrivateFileUrl(drcardHolding) : "";

        map.put("idcardFrontUrl", idcardFrontUrl);
        map.put("idcardBackUrl", idcardBackUrl);
        map.put("idcardHoldingUrl", idcardHoldingUrl);
        map.put("drcardFrontUrl", drcardFrontUrl);
        map.put("drcardBackUrl", drcardBackUrl);
        map.put("drcardHoldingUrl", drcardHoldingUrl);
        return map;
    }


    @Resource
    private OdrServiceApi odrServiceApi;

    @Override
    @Transactional
    @LcnTransaction
    public long registerNewDriver(RegisterNewDriverForm form) {
        R r = drServiceApi.registerNewDriver(form);
        return Convert.toLong(r.get("userId"));
    }

    @Override
    @Transactional
    @LcnTransaction
    public String createDriverFaceModel(CreateDriverFaceModelForm form) {
        R r = drServiceApi.createDriverFaceModel(form);
        String res = MapUtil.getStr(r, "result");
        return res;
    }

    @Override
    public HashMap searchDriverBaseInfo(SearchDriverBaseInfoForm form) {
        return (HashMap) drServiceApi.searchDriverBaseInfo(form).get("result");
    }

    @Override
    public HashMap searchWorkbenchData(long driverId) {
        SearchDriverTodayBusinessDataForm form_1 = new SearchDriverTodayBusinessDataForm();
        form_1.setDriverId(driverId);
        R r = odrServiceApi.searchDriverTodayBusinessData(form_1);
        HashMap order = (HashMap) r.get("result");

        SearchDriverSettingsForm form_2 = new SearchDriverSettingsForm();
        form_2.setDriverId(driverId);
        r = drServiceApi.searchDriverSettings(form_2);
        HashMap settings = (HashMap) r.get("result");

        HashMap result = new HashMap<>() {{
            put("business", order);
            put("settings", settings);
        }};
        return result;
    }

    @Override
    public HashMap login(LoginForm form) {
        R r = drServiceApi.login(form);
        return (HashMap) r.get("result");
    }

    @Override
    @Transactional
    @LcnTransaction
    public int updateDriverAuth(UpdateDriverAuthForm form) {
        R r = drServiceApi.updateDriverAuth(form);
        return Convert.toInt(r.get("rows"));
    }
}

