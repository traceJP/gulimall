package com.tracejp.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/19 20:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catelog2Vo {

    private String catelog1Id;

    private List<Catelog3Vo> catalog3List;

    private String id;

    private String name;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catelog3Vo {

        private String catelog2Id;

        private String id;

        private String name;

    }

}
