package org.example.controllers;

import org.example.models.Link_shorter;
import org.example.models.Review;
import org.example.models.Role;
import org.example.models.User;
import org.example.repositories.LinkShorterRepository;
import org.example.repositories.ReviewRepository;
import org.example.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@Controller
public class MainController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LinkShorterRepository linkShorterRepository;

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("title", "Main");
        return "homePage";
    }

    @GetMapping("/about")
    public String aboutUs(Model model) {
        model.addAttribute("title", "Про нас");
        return "aboutUs";
    }

    @GetMapping("/reviews")
    public String reviews(Model model) {
        Iterable<Review> reviews = reviewRepository.findAll();
        model.addAttribute("title", "Отзывы");
        model.addAttribute("name", reviews);
        return "reviews";
    }

    @GetMapping("/services")
    public String services(Model model) {
        Iterable<Link_shorter> linkShorter = linkShorterRepository.findAll();
        model.addAttribute("title", "Сократить ссылку");
        model.addAttribute("shortLinks", linkShorter);
        model.addAttribute("addLabel", servicesMessageText);
        servicesMessageText = "";
        return "services";
    }

    //переменная статуса в сокращателе ссылок
    private String servicesMessageText = "";

    @PostMapping("/services")
    public String shortLink(@RequestParam String full_link, @RequestParam String short_link,  Model model){
        Iterable<Link_shorter> linkShorter = linkShorterRepository.findAll();
        for (Link_shorter x : linkShorter) {
            if (x.getShort_link().equals(short_link)) {
                servicesMessageText = "Такое сокращение существует, выберите другое!";
                return "redirect:services";
            }
        }
        Link_shorter link_shorter = new Link_shorter(full_link, short_link);
        linkShorterRepository.save(link_shorter);
        servicesMessageText = "Готово!";
        return "redirect:services";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("title", "Contacts");
        return "contactPage";
    }

    @PostMapping("/reviews-add")
    public String reviewsAdd(@AuthenticationPrincipal User user, @RequestParam String title, @RequestParam String text, Model model) {
        Review review = new Review(title, text, user);
        reviewRepository.save(review);
        return "redirect:/reviews";
    }

    @GetMapping("/reviews/{id}")
    public String reviewInfo(@PathVariable(value = "id") long reviewId, Model model) {
        if (!reviewRepository.existsById(reviewId)){
            return "redirect:/reviews";
        }
        Optional<Review> review = reviewRepository.findById(reviewId);
        ArrayList<Review> result = new ArrayList<>();
        review.ifPresent(result::add);
        model.addAttribute("review", result);
        return "review-info";
    }

    @GetMapping("/reviews/{id}/update")
    public String reviewUpdate(@PathVariable(value = "id") long reviewId, Model model) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        ArrayList<Review> result = new ArrayList<>();
        review.ifPresent(result::add);
        model.addAttribute("review", result);
        return "review-update";
    }

    @PostMapping("/reviews/{id}/update")
    public String reviewUpdateForm(@PathVariable(value = "id") long reviewId, @RequestParam String title, @RequestParam String text, Model model) throws ClassNotFoundException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ClassNotFoundException());
        review.setTitle(title);
        review.setText(text);
        reviewRepository.save(review);
        return "redirect:/reviews/" + reviewId;
    }

    @PostMapping("/reviews/{id}/delete")
    public String reviewsDelete(@PathVariable(value = "id") long reviewId, Model model) throws ClassNotFoundException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ClassNotFoundException());
        reviewRepository.delete(review);
        return "redirect:/reviews";
    }

    @GetMapping("/reg")
    public String reg() {
        return "reg";
    }

    @PostMapping("/reg")
    public String addUser(User user, Model model) {
        user.setEnabled(true);
        user.setRoles(Collections.singleton(Role.USER));
        userRepository.save(user);
        return "redirect:/login";
    }

    @GetMapping("/user/")
    public String userCabinet(Model model, Principal principal) {
        String name = principal.getName(); //get logged in username
        User user = userRepository.findByUsername(name);
        model.addAttribute("email", user.getEmail());
        return "user-cabinet";
    }

    @PostMapping("/user/")
    public String userCabinetDataUpdate(User userForm, Model model, Principal principal) {
        String name = principal.getName();
        User user = userRepository.findByUsername(name);
        user.setEmail(userForm.getEmail());
        user.setPassword(userForm.getPassword());
        user.setRoles(userForm.getRoles());
        userRepository.save(user);
        return "redirect:/user/";
    }

    @GetMapping("/admin")
    public String allUsers(Model model) {
        Iterable<User> user = userRepository.findAll();
        model.addAttribute("users", user);
        return "admin-panel";
    }

    @GetMapping("/admin/user-{id}")
    public String oneUserReview(@PathVariable(value = "id") long id, Model model) {
        if (!userRepository.existsById(id)) {
            return "redirect:/admin";
        }
//---------------------------------------
//        Вариант 1 работает без  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "author")
//                                private Set<Review> reviews = new HashSet<>();
//
//        Iterable<Review> reviews = reviewRepository.findAll();
//        ArrayList<Review> reviewArrayList = new ArrayList<>();
//        for (Review x: reviews){
//            long authorId = x.getAuthor().getId();
//            if (authorId == id)
//                reviewArrayList.add(x);
//        }
//        model.addAttribute("userReviews", reviewArrayList);
//----------------------------------------
//        Вариант 2 тоже работает только с @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "author")
//                                         private Set<Review> reviews = new HashSet<>();

        Optional<User> user = userRepository.findById(id);
        Set<Review> reviews = user.get().getReviews();
        model.addAttribute("userReviews", reviews);
        return "user-reviews";
    }

}
