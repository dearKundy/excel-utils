package com.kundy.excelutils.mapper;


import com.kundy.excelutils.entity.po.TtlProductInfoPo;

import java.util.List;
import java.util.Map;

/**
 * @author kundy
 * @create 2019/2/16 10:42 AM
 */
public interface TtlProductInfoMapper {

    List<TtlProductInfoPo> listProduct(Map<String, Object> map);

}
