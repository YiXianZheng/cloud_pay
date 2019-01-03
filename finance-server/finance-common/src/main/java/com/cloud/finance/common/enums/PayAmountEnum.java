package com.cloud.finance.common.enums;

import lombok.Getter;

@Getter
public enum PayAmountEnum {

    PAY_TEN(1000),
    PAY_TWENTY(2000),
    PAY_THIRTY(3000),
    PAY_FIFTY(5000),
    PAY_HUNDRED(10000)
    ;

    private Integer amount;

    PayAmountEnum(Integer amount) {
        this.amount = amount;
    }

    public static boolean checkAmount(Integer amount) {

        if(amount == null) return false;
        for (PayAmountEnum payAmount : PayAmountEnum.values()){
            if(amount.equals(payAmount.getAmount())){
                return true;
            }
        }
        return false;
    }
}
