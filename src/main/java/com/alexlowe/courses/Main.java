package com.alexlowe.courses;

import com.alexlowe.courses.model.CourseIdea;
import com.alexlowe.courses.model.CourseIdeaDAO;
import com.alexlowe.courses.model.SimpleCourseIdeaDAO;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * Created by Keyes on 6/18/2016.
 */
public class Main {
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
            //TODO: send msg about redirect
            if(req.attribute("username") == null){
                res.redirect("/");
                halt();
            }
        });
        get("/", (req, res) -> {
            Map<String,String> model = new HashMap<>();
            model.put("username", req.attribute("username"));
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
            idea.addVoter(req.attribute("username"));
            res.redirect("/ideas");
            return null;
        });
    }
}
