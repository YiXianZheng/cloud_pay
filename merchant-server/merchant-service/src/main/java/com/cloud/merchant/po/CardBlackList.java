package com.cloud.merchant.po;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @description: ${description}
 * @author: zyx
 * @create: 2019-05-04 12:33
 **/
@Entity
@Data
public class CardBlackList {

    @Id
    private String id;

    private String bankCardHolder;

    private String bankCardNo;

    private String bankName;
}
