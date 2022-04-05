package com.onpost.domain.service;

import com.onpost.domain.dto.auth.LoginDto;
import com.onpost.domain.dto.auth.SignupDto;
import com.onpost.domain.dto.auth.TokenDto;
import com.onpost.domain.entity.member.Authority;
import com.onpost.domain.entity.member.Member;
import com.onpost.domain.repository.MemberQueryRepository;
import com.onpost.global.error.exception.EmailAlreadyExistsException;
import com.onpost.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Random;

@Service
@Slf4j
@Transactional(rollbackFor = {Exception.class})
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberQueryRepository memberQueryRepository;
    private final PasswordEncoder passwordEncoder;

    public Member signupMember(SignupDto signupDto) {

        if (memberQueryRepository.checkEmail(signupDto.getEmail())) {
            throw EmailAlreadyExistsException.EXCEPTION;
        }

        Member.MemberBuilder builder = Member.builder()
                .follower(new LinkedHashSet<>())
                .following(new LinkedHashSet<>())
                .makePost(new LinkedList<>())
                .email(signupDto.getEmail())
                .name(signupDto.getUsername())
                .password(passwordEncoder.encode(signupDto.getPassword()))
                .certified(certifiedKey());

        if (signupDto.getEmail().equals("khcho0125@dsm.hs.kr")) {
            builder.author(Authority.ADMIN);
        } else {
            builder.author(Authority.USER);
        }

        return memberQueryRepository.create(builder.build());
    }

    public TokenDto loginMember(LoginDto loginDto) {


        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        String authorites = jwtProvider.getAuthorities(authentication);

        return jwtProvider.generateToken(loginDto.getEmail(), authorites);
    }

    private String certifiedKey() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        int num, len = 0;

        do {
            num = random.nextInt(75) + 48;
            if ( (num <= 57) || (num >= 65 && num <= 90) || (num >= 97)) {
                sb.append((char) num);
                len++;
            }

        } while (len < 10);
        return sb.toString();

    }
}
