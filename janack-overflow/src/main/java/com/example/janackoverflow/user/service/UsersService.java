package com.example.janackoverflow.user.service;

import com.example.janackoverflow.user.domain.request.UsersRequestDTO;
import com.example.janackoverflow.user.domain.response.UsersResponseDTO;
import com.example.janackoverflow.user.entity.Users;
import com.example.janackoverflow.user.repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsersService {
    private final UsersRepository usersRepository;

    public UsersService(UsersRepository usersRepository){
        this.usersRepository = usersRepository;
    }

    public boolean isDuplicatedNick(UsersRequestDTO usersRequestDTO){ //닉네임 중복 확인
        Users users = usersRequestDTO.toEntity();
        Optional<Users> optionalUsers = usersRepository.findByNickname(users.getNickname());
        return optionalUsers.isPresent() && !(optionalUsers.get().getId() == users.getId());
    }

    public boolean isDuplicatedEmail(UsersRequestDTO usersRequestDTO){ //이메일 중복 확인
        Users users = usersRequestDTO.toEntity();
        Optional<Users> optionalUsers = usersRepository.findByEmail(users.getEmail());
        return optionalUsers.isPresent() && !(optionalUsers.get().getId() == users.getId());
    }

    public Users createUser(UsersRequestDTO usersRequestDTO){ //회원 생성
        Users users = usersRequestDTO.toEntity();
        return usersRepository.save(users);
    }

    public UsersResponseDTO readUser(long id){ //회원 하나 읽기
        Users users = usersRepository.findById(id).orElseThrow(RuntimeException::new);
        return UsersResponseDTO.builder()
                .id(id)
                .email(users.getEmail())
                .digit(users.getDigit())
                .birth(users.getBirth())
                .name(users.getName())
                .createdAt(users.getCreatedAt())
                .nickname(users.getNickname())
                .profileImage(users.getProfileImage())
                .holder(users.getHolder())
                .bankName(users.getBankName())
                .outputAcntNum(users.getOutputAcntNum())
                .build();
    }

//    public List<Users> readAllUser(){ //회원 전체 읽기
//        List<Users> list = usersRepository.findAll();
//
//    }

    public void updateUser(UsersRequestDTO usersRequestDTO, long id){ //회원 정보 수정
        Users users = usersRepository.findById(id).orElseThrow(RuntimeException::new);
        if(users.getPassword().equals(usersRequestDTO.getPassword())){
            Users updatedUser = users.toBuilder()
                    .email(usersRequestDTO.getEmail())
                    .digit(usersRequestDTO.getDigit())
                    .birth(usersRequestDTO.getBirth())
                    .name(usersRequestDTO.getName())
                    .nickname(usersRequestDTO.getNickname())
                    .holder(usersRequestDTO.getHolder())
                    .bankName(usersRequestDTO.getBankName())
                    .outputAcntNum(usersRequestDTO.getOutputAcntNum())
                    .build();
            if(usersRequestDTO.getNewPassword() != null){
                if(usersRequestDTO.getNewPassword().equals(usersRequestDTO.getNewPasswordConfirm())){
                    updatedUser.updatePassword(usersRequestDTO.getNewPassword());
                }else{
                    throw new RuntimeException("패스워드와 패스워드확인이 다릅니다.");
                }
            }
            usersRepository.save(updatedUser);
        }else{
            throw new RuntimeException("패스워드가 틀렸습니다.");
        }
    }

    public void updateProfileImage(UsersRequestDTO usersRequestDTO, long id){ // 프로필사진만 교체
        Users users = usersRepository.findById(id).orElseThrow(RuntimeException::new);
        Users updatedUser = users.toBuilder()
                .profileImage(usersRequestDTO.getProfileImage())
                .build();
        usersRepository.save(updatedUser);
    }
}