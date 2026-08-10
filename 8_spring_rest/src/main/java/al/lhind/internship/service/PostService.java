package al.lhind.internship.service;

import al.lhind.internship.model.Post;
import al.lhind.internship.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post findPostByTitle(String title) {
        Post post = postRepository.findPostByTitle(title);
        if (post == null) {
            throw new RuntimeException("Post not found with title: " + title);
        }
        return post;
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    public void deletePostById(Long id) {
        getPostById(id);
        postRepository.deleteById(id);
    }
}