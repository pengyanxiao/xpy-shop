package com.baidu.web;

import com.baidu.service.PageService;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @ClassName PageController
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/9/24
 * @Version V1.0
 **/
//@Controller
//@RequestMapping(value = "item")
public class PageController {

    //@Autowired
    private PageService pageService;

    //@GetMapping(value = "{spuId}.html")
    public String test(@PathVariable(value = "spuId") Integer spuId , ModelMap modelMap){

        Map<String, Object> map = pageService.getPageInfoBySupId(spuId);
        modelMap.putAll(map);
        return "item";
    }

}
