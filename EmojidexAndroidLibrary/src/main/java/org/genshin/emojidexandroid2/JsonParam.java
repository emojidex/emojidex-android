package org.genshin.emojidexandroid2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kou on 14/10/10.
 */
class JsonParam extends SimpleJsonParam {
    @JsonProperty("code_ja")    protected String name_ja = null;
    @JsonProperty("checksums")  protected Checksums checksums = null;

    public static class Checksums
    {
        @JsonProperty("svg")   public String svg = null;
        @JsonProperty("png")   public HashMap<String, String> png = null;
    }
}
