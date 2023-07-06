package com.olympus.olympus.framework.controller;

import com.olympus.olympus.framework.dto.ResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/login")
public class loginController {

    @GetMapping("/loginStatus.do")
    public @ResponseBody ResponseDTO loginStatus(HttpServletRequest req) {
        HttpSession session = req.getSession();

        ResponseDTO result = new ResponseDTO();

        String memberId = (String) session.getAttribute("memberId");
        memberId = memberId == null ? "" : memberId;

        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", "");
        map.put("memberId", memberId);
        result.setData(map);

        return result;
    }

    @PostMapping("/login.do")
    public @ResponseBody ResponseDTO login(HttpServletRequest req) {
        String id = req.getParameter("id");
        String password = req.getParameter("password");

        ResponseDTO result = new ResponseDTO();

        //sso 로직 넣어서 로그인 체크하여 성공시
        Boolean loginCheck = true;
        if (loginCheck) {
            HttpSession session = req.getSession();
            session.setAttribute("memberId", id);

            result.setResultCd("true");
            result.setResultMsg("로그인에 성공하였습니다.");
        } else {
            HttpSession session = req.getSession();
            session.setAttribute("memberId", "");

            result.setResultCd("false");
            result.setResultMsg("로그인에 실페히였습니다.");
        }

        return result;
    }

    @PostMapping("/loginout.do")
    public @ResponseBody ResponseDTO logout(HttpServletRequest req) {

        ResponseDTO result = new ResponseDTO();

        HttpSession session = req.getSession();
        session.invalidate();

        result.setResultCd("true");
        result.setResultMsg("정상적으로 로그아웃 되었습니다.");

        return result;
    }

}
