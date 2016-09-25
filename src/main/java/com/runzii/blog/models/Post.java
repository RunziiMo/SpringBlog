package com.runzii.blog.models;

import com.runzii.blog.models.support.PostFormat;
import com.runzii.blog.models.support.PostStatus;
import com.runzii.blog.models.support.PostType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Entity
@Table(name = "posts")
@Getter @Setter
public class Post extends BaseModel{
    private static final SimpleDateFormat SLUG_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

    @ManyToOne
    private User user;

    @Column(nullable = false)
    private String title;

    @Type(type="text")
    private String content;

    @Type(type = "text")
    private String renderedContent;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PostStatus postStatus = PostStatus.PUBLISHED;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PostFormat postFormat = PostFormat.MARKDOWN;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PostType postType = PostType.POST;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "posts_tags",
            joinColumns = {@JoinColumn(name = "post_id", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "tag_id", nullable = false, updatable = false)}
    )
    private Set<Tag> tags = new HashSet<>();

    private String permalink;

    public String getRenderedContent() {
        if (this.postFormat == PostFormat.MARKDOWN)
            return renderedContent;

        return getContent();
    }

    public void setPermalink(String permalink){
        String token = permalink.toLowerCase().replace("\n", " ").replaceAll("[^a-z\\d\\s]", " ");
        this.permalink = StringUtils.arrayToDelimitedString(StringUtils.tokenizeToStringArray(token, " "), "-");
    }

    public String getContent() {
        return content;
    }

    public PostFormat getPostFormat() {
        return postFormat;
    }

    public void setPostFormat(PostFormat postFormat) {
        this.postFormat = postFormat;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setRenderedContent(String renderedContent) {
        this.renderedContent = renderedContent;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


}
