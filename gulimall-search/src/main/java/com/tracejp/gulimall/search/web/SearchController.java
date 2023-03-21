package com.tracejp.gulimall.search.web;

import com.tracejp.gulimall.search.service.MallSearchService;
import com.tracejp.gulimall.search.vo.SearchParam;
import com.tracejp.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/20 19:35
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;


    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request) {

        String queryString = request.getQueryString();
        searchParam.set_queryString(queryString);

        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);

        return "list";
    }


}
