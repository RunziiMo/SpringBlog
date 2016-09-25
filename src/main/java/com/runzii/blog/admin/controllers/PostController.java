package com.runzii.blog.admin.controllers;

import com.runzii.blog.forms.PostForm;
import com.runzii.blog.models.Post;
import com.runzii.blog.models.support.PostFormat;
import com.runzii.blog.models.support.PostStatus;
import com.runzii.blog.repositories.PostRepository;
import com.runzii.blog.repositories.UserRepository;
import com.runzii.blog.services.PostService;
import com.runzii.blog.utils.DTOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.validation.Valid;
import java.security.Principal;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Controller("adminPostController")
@RequestMapping("admin/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    private static final int PAGE_SIZE = 20;

    @RequestMapping(value = "")
    public String index(@RequestParam(defaultValue = "0") int page, Model model){
        Page<Post> posts = postRepository.findAll(new PageRequest(page, PAGE_SIZE, Sort.Direction.DESC, "id"));

        model.addAttribute("totalPages", posts.getTotalPages());
        model.addAttribute("page", page);
        model.addAttribute("posts", posts);

        return "admin/posts/index";
    }

    @RequestMapping(value = "new")
    public String newPost(Model model){
        PostForm postForm = DTOUtil.map(new Post(), PostForm.class);
        postForm.setPostTags("");

        model.addAttribute("postForm", postForm);
        model.addAttribute("postFormats", PostFormat.values());
        model.addAttribute("postStatus", PostStatus.values());

        return "admin/posts/new";
    }

    @RequestMapping(value = "{postId:[0-9]+}/edit")
    public String editPost(@PathVariable Long postId, Model model){
        Post post = postRepository.findOne(postId);
        PostForm postForm = DTOUtil.map(post, PostForm.class);

        postForm.setPostTags(postService.getTagNames(post.getTags()));

        model.addAttribute("post", post);
        model.addAttribute("postForm", postForm);
        model.addAttribute("postFormats", PostFormat.values());
        model.addAttribute("postStatus", PostStatus.values());

        return "admin/posts/edit";
    }

    @RequestMapping(value = "{postId:[0-9]+}/delete", method = {DELETE, POST})
    public String deletePost(@PathVariable Long postId){
        postService.deletePost(postRepository.findOne(postId));
        return "redirect:/admin/posts";
    }

    @RequestMapping(value = "", method = POST)
    public String create(Principal principal, @Valid PostForm postForm, Errors errors, Model model){
        if (errors.hasErrors()) {
            model.addAttribute("postFormats", PostFormat.values());
            model.addAttribute("postStatus", PostStatus.values());

            return "admin/posts/new";
        } else {
            Post post = DTOUtil.map(postForm, Post.class);
            post.setUser(userRepository.findByEmail(principal.getName()));
            post.setTags(postService.parseTagNames(postForm.getPostTags()));

            postService.createPost(post);

            return "redirect:/admin/posts";
        }
    }

    @RequestMapping(value = "{postId:[0-9]+}", method = {PUT, POST})
    public String update(@PathVariable Long postId, @Valid PostForm postForm, Errors errors, Model model){
        if (errors.hasErrors()){
            model.addAttribute("postFormats", PostFormat.values());
            model.addAttribute("postStatus", PostStatus.values());

            return "admin/posts_edit";
        } else {
            Post post = postRepository.findOne(postId);
            DTOUtil.mapTo(postForm, post);
            post.setTags(postService.parseTagNames(postForm.getPostTags()));

            postService.updatePost(post);

            return "redirect:/admin/posts";
        }
    }

}
