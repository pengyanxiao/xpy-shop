package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.mapper.SpecGroupMapper;
import com.baidu.shop.service.SpecificationServer;
import com.baidu.shop.utils.BaiduBrandUtil;
import com.baidu.shop.utils.ObjectUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName SpecGroupServiceImpl
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/9/3
 * @Version V1.0
 **/
@RestController
public class SpecGroupServiceImpl extends BaseApiService implements SpecificationServer {

    @Resource
   private SpecGroupMapper specGroupMapper;

    @Override
    public Result<List<SpecGroupEntity>> getSpecGroup(SpecGroupDTO specGroupDTO) {

        //通过分类id查询数据
        Example example = new Example(SpecGroupEntity.class);

        if(ObjectUtil.isNotNull(specGroupDTO.getCid())) example.createCriteria().andEqualTo("cid",specGroupDTO.getCid());

        List<SpecGroupEntity> specGroupEntities = specGroupMapper.selectByExample(example);

        return this.setResultSuccess(specGroupEntities);
    }

    @Transactional
    @Override
    public Result<JSONObject> save(SpecGroupDTO specGroupDTO) {

        specGroupMapper.insertSelective(BaiduBrandUtil.copyProperties(specGroupDTO,SpecGroupEntity.class));

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> edit(SpecGroupDTO specGroupDTO) {

        specGroupMapper.updateByPrimaryKeySelective(BaiduBrandUtil.copyProperties(specGroupDTO,SpecGroupEntity.class));

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> delete(Integer id) {
        specGroupMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }


}
