package com.tracejp.common.constant;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/9 20:00
 */
public class ProductConstant {

    public enum AttrEnum {

        ATTR_TYPE_BASE(1, "基本属性"),

        ATTR_TYPE_SALE(0, "销售属性");

        private Integer code;

        private String msg;

        AttrEnum(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

    }

    public enum StatusEnum {

        NEW_SPU(0, "新建"),

        SPU_UP(1, "商品上架"),

        SUP_DOWN(2, "商品下架");

        private Integer code;

        private String msg;

        StatusEnum(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

    }


}
