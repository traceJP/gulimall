package com.tracejp.gulimall.search.service;

import com.tracejp.gulimall.search.vo.SearchParam;
import com.tracejp.gulimall.search.vo.SearchResult;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/20 19:37
 */
public interface MallSearchService {

    SearchResult search(SearchParam param);

}
