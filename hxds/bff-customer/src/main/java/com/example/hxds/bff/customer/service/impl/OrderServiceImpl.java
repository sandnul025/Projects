package com.example.hxds.bff.customer.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import com.example.hxds.bff.customer.controller.form.CreateNewOrderForm;
import com.example.hxds.bff.customer.controller.form.EstimateOrderChargeForm;
import com.example.hxds.bff.customer.controller.form.EstimateOrderMileageAndMinuteForm;
import com.example.hxds.bff.customer.controller.form.InsertOrderForm;
import com.example.hxds.bff.customer.feign.MpsServiceApi;
import com.example.hxds.bff.customer.feign.OdrServiceApi;
import com.example.hxds.bff.customer.feign.RuleServiceApi;
import com.example.hxds.bff.customer.service.OrderService;
import com.example.hxds.common.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Resource
    private OdrServiceApi odrServiceApi;

    @Resource
    private MpsServiceApi mpsServiceApi;

    @Resource
    private RuleServiceApi ruleServiceApi;

    @Resource
    private SnmServiceApi snmServiceApi;

    @Override
    @Transactional
    @LcnTransaction
    public int createNewOrder(CreateNewOrderForm form) {
        Long customerId = form.getCustomerId();
        String startPlace = form.getStartPlace();
        String startPlaceLatitude = form.getStartPlaceLatitude();
        String startPlaceLongitude = form.getStartPlaceLongitude();
        String endPlace = form.getEndPlace();
        String endPlaceLatitude = form.getEndPlaceLatitude();
        String endPlaceLongitude = form.getEndPlaceLongitude();
        String favourFee = form.getFavourFee();
        /**
         * 【重新预估里程和时间】
         * 虽然下单前，系统会预估里程和时长，但是有可能顾客在下单页面停留时间过长，
         * 然后再按下单键，这时候路线和时长可能都有变化，所以需要重新预估里程和时间
         */
        EstimateOrderMileageAndMinuteForm form_1 = new EstimateOrderMileageAndMinuteForm();
        form_1.setMode("driving");
        form_1.setStartPlaceLatitude(startPlaceLatitude);
        form_1.setStartPlaceLongitude(startPlaceLongitude);
        form_1.setEndPlaceLatitude(endPlaceLatitude);
        form_1.setEndPlaceLongitude(endPlaceLongitude);
        R r = mpsServiceApi.estimateOrderMileageAndMinute(form_1);
        HashMap map = (HashMap) r.get("result");
        String mileage = MapUtil.getStr(map, "mileage");
        int minute = MapUtil.getInt(map, "minute");

        /**
         * 重新估算订单金额
         */
        EstimateOrderChargeForm form_2 = new EstimateOrderChargeForm();
        form_2.setMileage(mileage);
        form_2.setTime(new DateTime().toTimeStr());
        r = ruleServiceApi.estimateOrderCharge(form_2);
        map = (HashMap) r.get("result");
        String expectsFee = MapUtil.getStr(map, "amount");
        String chargeRuleId = MapUtil.getStr(map, "chargeRuleId");
        short baseMileage = MapUtil.getShort(map, "baseMileage");
        String baseMileagePrice = MapUtil.getStr(map, "baseMileagePrice");
        String exceedMileagePrice = MapUtil.getStr(map, "exceedMileagePrice");
        short baseMinute = MapUtil.getShort(map, "baseMinute");
        String exceedMinutePrice = MapUtil.getStr(map, "exceedMinutePrice");
        short baseReturnMileage = MapUtil.getShort(map, "baseReturnMileage");
        String exceedReturnPrice = MapUtil.getStr(map, "exceedReturnPrice");

        /**
         * 生成订单记录
         */
        InsertOrderForm form_4 = new InsertOrderForm();
        //UUID字符串，充当订单号，微信支付时候会用上
        form_4.setUuid(IdUtil.simpleUUID());
        form_4.setCustomerId(customerId);
        form_4.setStartPlace(startPlace);
        form_4.setStartPlaceLatitude(startPlaceLatitude);
        form_4.setStartPlaceLongitude(startPlaceLongitude);
        form_4.setEndPlace(endPlace);
        form_4.setEndPlaceLatitude(endPlaceLatitude);
        form_4.setEndPlaceLongitude(endPlaceLongitude);
        form_4.setExpectsMileage(mileage);
        form_4.setExpectsFee(expectsFee);
        form_4.setFavourFee(favourFee);
        form_4.setDate(new DateTime().toDateStr());
        form_4.setChargeRuleId(Long.parseLong(chargeRuleId));
        form_4.setCarPlate(form.getCarPlate());
        form_4.setCarType(form.getCarType());
        form_4.setBaseMileage(baseMileage);
        form_4.setBaseMileagePrice(baseMileagePrice);
        form_4.setExceedMileagePrice(exceedMileagePrice);
        form_4.setBaseMinute(baseMinute);
        form_4.setExceedMinutePrice(exceedMinutePrice);
        form_4.setBaseReturnMileage(baseReturnMileage);
        form_4.setExceedReturnPrice(exceedReturnPrice);

        r = odrServiceApi.insertOrder(form_4);
        String orderId = MapUtil.getStr(r, "result");

        return 0;

    }

}

