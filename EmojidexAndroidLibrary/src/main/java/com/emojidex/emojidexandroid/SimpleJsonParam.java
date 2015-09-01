package com.emojidex.emojidexandroid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by kou on 14/10/10.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class SimpleJsonParam {
    @JsonProperty("code")       protected String name = null;
    @JsonProperty("moji")       protected String text = null;
    @JsonProperty("category")   protected String category = null;
    @JsonProperty("variants")   protected List<String> variants = null;
    @JsonProperty("base")       protected String base = null;
}
