package com.snelson.cadenceAPI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FallbackController {

    @RequestMapping(value = "/{path:^(?!.*\\..*).*}")
    public String redirect() {
        return "forward:/";
    }
}


