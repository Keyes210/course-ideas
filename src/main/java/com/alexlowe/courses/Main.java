package com.alexlowe.courses;

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

    }
}
