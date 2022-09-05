package com.ll.exam.qsl.user.entity;

import com.ll.exam.qsl.interestKeyword.entity.InterestKeyword;
import lombok.*;

import javax.persistence.*;
import java.util.Collection;
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

    // 팔로잉
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<SiteUser> followings = new HashSet<>();

    public void addInterestKeywordContent(String keywordContent) {
        interestKeywords.add(new InterestKeyword(this, keywordContent));
    }

    public void follow(SiteUser following) {
        if (this == following) return;      // 같은 사람이면 팔로우 X
        if (following == null) return;      // null이 들어오면 X
        if (this.getId() == following.getId()) return;      // Id가 같다면 팔로우 X

        // 유튜버(following)이 나(follower)를 구독자로 등록
        following.getFollowers().add(this);

        // 내(follower)가 유튜버(following)를 구독한다.
        getFollowings().add(following);
    }
}