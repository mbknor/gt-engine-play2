package controllers;

import play.api.templates.Html;
import play.core.Router;
import play.data.Form;
import play.data.format.Formats;
import play.data.validation.Constraints;
import kjetland.gtengineplay.gte;
import play.i18n.Messages;
import play.mvc.*;
import views.html.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application extends Controller {
  
    public static class Data {
        public String id;

        @Constraints.Required
        public String name;

        public Data() {
        }

        public Data(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static Result index() {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "Morten");

        //int q = 1/ 0;

        List<Integer> myList = Arrays.asList(1,2,3,4,5);
        params.put("myList", myList);
        
        List<Data> dataList = new ArrayList<Data>();
        dataList.add( new Data("a", "Book"));
        dataList.add( new Data("b", "Car"));
        params.put("dataList", dataList);



        
        return ok(gte
                .template("Application/index.html")
                .addParam("today", new Date())
                .render(params));
    }
    
    public static Result someOtherPage(String input) {
        return ok(new Html("other page: " + input));
    }

    
    public static Result input1() {
        Form<Data> form = form(Data.class).bindFromRequest();

        System.out.println("errors: " + form.hasErrors() );
        return ok( gte.template("Application/input1.html")
                .withForm(form)
                .addParam("is_error", form.hasErrors())
                .render() );
    }
  
}