package com.ll.exam.qsl.user.entity;

import com.ll.exam.qsl.interestKeyword.entity.InterestKeyword;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String email;

    // 관심사
    @Builder.Default    // null이 되는걸 막기 위해 추가
    @ManyToMany(cascade = CascadeType.ALL)      // ManyToMany 관계
    private Set<InterestKeyword> interestKeywords = new HashSet<>();

    // 팔로워
    @Builder.Default
    @ManyToMany(cascade = CascadeType.ALL)
    private Set<SiteUser> followers = new HashSet<>();

    public void addInterestKeywordContent(String keywordContent) {
        interestKeywords.add(new InterestKeyword(keywordContent));
    }

    public void follow(SiteUser following) {
        following.getFollowers().add(this);
    }
}