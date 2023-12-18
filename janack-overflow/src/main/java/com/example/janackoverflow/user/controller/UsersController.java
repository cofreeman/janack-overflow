package com.example.janackoverflow.user.controller;

import com.example.janackoverflow.global.security.DTO.MailDTO;
import com.example.janackoverflow.global.security.Service.MailService;
import com.example.janackoverflow.global.security.auth.NowUserDetails;
import com.example.janackoverflow.user.domain.request.UsersRequestDTO;
import com.example.janackoverflow.user.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;

@RestController
public class UsersController {
    private final UsersService usersService;

    @Autowired
    private MailService mailService;

    public UsersController(UsersService usersService){
        this.usersService = usersService;
    }

//    회원가입
    @PostMapping("/signup")
    public ResponseEntity createUser(@RequestBody UsersRequestDTO usersRequestDTO){

        if(usersRequestDTO.getEmail().isEmpty()
        || usersRequestDTO.getPassword().isEmpty()
        || usersRequestDTO.getName().isEmpty()
        || usersRequestDTO.getNickname().isEmpty()
        || usersRequestDTO.getDigit().isEmpty()
        || usersRequestDTO.getBirth() == null){
            return new ResponseEntity<>("필수 입력 항목입니다", HttpStatus.FORBIDDEN);
        }

        if (usersService.isDuplicatedNick(usersRequestDTO)) { //닉네임 중복확인
            return new ResponseEntity<>("중복되는 닉네임입니다", HttpStatus.FORBIDDEN);
        } else if(usersService.isDuplicatedEmail(usersRequestDTO)) { //이메일 중복확인
            return new ResponseEntity<>("중복되는 이메일입니다", HttpStatus.FORBIDDEN);
        } else {
            usersRequestDTO.setRole("USER"); //역활
            usersRequestDTO.setStatus("01"); //상태
            usersRequestDTO.setProfileImage("default.png"); //프로필 이미지

            usersService.createUser(usersRequestDTO);

            return new ResponseEntity<>("성공적으로 생성", HttpStatus.OK);
        }

    }

//    이메일 인증
    @PostMapping("/mailPass")
    public ResponseEntity mailPass(@RequestBody UsersRequestDTO usersRequestDTO){

        //메일 주소 유효성 검사
        try {
            usersService.findByEmail(usersRequestDTO.getEmail());
        } catch (Exception e){
            return new ResponseEntity<>("입력하신 메일과 일치하는 회원이 없습니다.", HttpStatus.FORBIDDEN);
        }

        //입력한 메일이 유효하면, 임시 번호를 생성하고
        int randNum = (int) (Math.random() * 9999) + 999;
        String tempPass = "temp" + randNum;

        //DB에 업데이트 (서비스에서 암호화)
        usersService.updateRandomPass(usersRequestDTO, tempPass);

        //발급한 임시 번호를 입력받은 메일로 발송
        MailDTO emailMessage = MailDTO.builder()
                .to(usersRequestDTO.getEmail()) //작성한 email
                .subject("임시 비밀번호 입니다")
                .message("설정된 임시 비밀번호는 " + tempPass + " 입니다.")
                .build();

        if(mailService.sendMail(emailMessage)){
            return new ResponseEntity<>("회원님의 메일로 임시 비밀번호가 발송되었습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("메일 발송에 실패했습니다..", HttpStatus.FORBIDDEN);
        }

    }

}
