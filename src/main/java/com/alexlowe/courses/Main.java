package com.alexlowe.courses;

import com.alexlowe.courses.model.CourseIdea;
import com.alexlowe.courses.model.CourseIdeaDAO;
import com.alexlowe.courses.model.SimpleCourseIdeaDAO;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

/**
 * Created by Keyes on 6/18/2016.
 */
public class Main {
    public static void main(String[] args) {
        staticFileLocation("/public");
        CourseIdeaDAO dao = new SimpleCourseIdeaDAO(); /*interface and implementation*/
        //this implementation is just for prototyping and will not survive server restart, for persistence, need a db

        get("/", (req, res) -> {
            Map<String,String> model = new HashMap<>();
            model.put("username", req.cookie("username"));
            return new ModelAndView(model, "index.hbs");
        },  new HandlebarsTemplateEngine());

        post("/sign-in", (req, res) -> {
            Map<String,String> model = new HashMap<>();
            String username = req.queryParams("username");
            res.cookie("username", username);
            model.put("username", username);
            return new ModelAndView(model, "sign-in.hbs");
        },  new HandlebarsTemplateEngine());

        get("/ideas", (req, res) -> { /* ** */
            Map<String,Object> model = new HashMap<>();
            model.put("ideas", dao.findAll()); /*passing in list of CourseIdeas, will use in ideas.hbs*/
            return new ModelAndView(model, "ideas.hbs");
        }, new HandlebarsTemplateEngine());

        post("/ideas", (req, res) -> {
            String title = req.queryParams("title");
            //TODO:username is tied to cookie implementation, want to change that;
            CourseIdea courseIdea = new CourseIdea(title, req.queryParams("username"));
            dao.add(courseIdea);
            res.redirect("ideas"); /*refresh page*/
            return null;
        },  new HandlebarsTemplateEngine());
    }
}
