package com.alexlowe.courses;

import com.alexlowe.courses.model.CourseIdea;
import com.alexlowe.courses.model.CourseIdeaDAO;
import com.alexlowe.courses.model.NotFoundException;
import com.alexlowe.courses.model.SimpleCourseIdeaDAO;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.nio.channels.NotYetBoundException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * Created by Keyes on 6/18/2016.
 */
public class Main {

    private static final String FLASH_MESSAGE_KEY = "flash_message";

    public static void main(String[] args) {
        staticFileLocation("/public");
        CourseIdeaDAO dao = new SimpleCourseIdeaDAO(); /*interface and implementation*/
        //this implementation is just for prototyping and will not survive server restart, for persistence, need a db

        before((req, res) -> {
            if(req.cookie("username") != null){
                req.attribute("username", req.cookie("username"));
            }
        });

        before("/ideas", (req, res) -> {
            if(req.attribute("username") == null){
                setFlashMessage(req, "Whoops, please sign in first.");
                res.redirect("/");
                halt();
            }
        });
        get("/", (req, res) -> {
            Map<String,String> model = new HashMap<>();
            model.put("username", req.attribute("username"));
            model.put("flashMessage", captureFlashMessage(req));
            return new ModelAndView(model, "index.hbs");
        },  new HandlebarsTemplateEngine());

        post("/sign-in", (req, res) -> {
            Map<String,String> model = new HashMap<>();
            String username = req.queryParams("username");
            res.cookie("username", username);
            res.redirect("/");
            return null;
        });

        get("/ideas", (req, res) -> { /* ** */
            Map<String,Object> model = new HashMap<>();
            model.put("ideas", dao.findAll()); /*passing in list of CourseIdeas, will use in ideas.hbs*/
            model.put("flashMessage", captureFlashMessage(req));
            return new ModelAndView(model, "ideas.hbs");
        }, new HandlebarsTemplateEngine());

        post("/ideas", (req, res) -> {
            String title = req.queryParams("title");
            CourseIdea courseIdea = new CourseIdea(title, req.queryParams("username"));
            dao.add(courseIdea);
            res.redirect("/ideas"); /*refresh page*/
            return null;
        });

        get("/ideas/:slug", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("idea", dao.findBySlug(req.params("slug")));
            return new ModelAndView(model, "idea.hbs");
        }, new HandlebarsTemplateEngine());

        post("/ideas/:slug/vote", (req, res) -> {
            CourseIdea idea = dao.findBySlug(req.params("slug")); //this pulls whats in :slug
            boolean voteAdded = idea.addVoter(req.attribute("username"));
            if(voteAdded){
                setFlashMessage(req, "Thanks for your vote!");
            }else{
                setFlashMessage(req, "You already voted.");
            }
            res.redirect("/ideas");
            return null;
        });

        exception(NotFoundException.class, (exc, req, res) -> {
            res.status(404);
            HandlebarsTemplateEngine engine = new HandlebarsTemplateEngine();
            String html = engine.render(new ModelAndView(null, "not-found.hbs"));
            res.body(html);
        });
    }

    private static void setFlashMessage(Request req, String msg) {
        req.session().attribute(FLASH_MESSAGE_KEY, msg);
    }

    //make sure we don't create a session if we're not going to use it
    private static String getFlashMessage(Request req) {
        if (req.session(false) == null){ //no flash message to get
            return null;
        }

        //if the session already exists, we want to make sure our key exists
        if(!req.session().attributes().contains(FLASH_MESSAGE_KEY)){
            return null;
        }

        //if it does exist, we can pop it out
        return (String) req.session().attribute(FLASH_MESSAGE_KEY);
    }

    private static String captureFlashMessage(Request req) {
        String message = getFlashMessage(req);

        //see if f-m exists, and remove it if it does
        if(message != null){
            req.session().removeAttribute(FLASH_MESSAGE_KEY);
        }
        return message;
    }
}
